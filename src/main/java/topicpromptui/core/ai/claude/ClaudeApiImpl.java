package topicpromptui.core.ai.claude;

import com.google.gson.Gson;
import topicpromptui.core.ai.AiApi;
import topicpromptui.core.ai.AiApiException;
import topicpromptui.core.ai.AiResponse;
import topicpromptui.core.ai.ConversationTurn;
import topicpromptui.core.ai.SseParser;
import topicpromptui.core.ai.ToolCalls;
import topicpromptui.core.config.ConfigModel;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

// ClaudeModule builds this instance manually (new ClaudeApiImpl(model, effort, context7Enabled)) and binds it via
// toInstance(...) so the hardcoded model/effort constants stay per-binding; Guice therefore never
// calls this constructor and can only supply configModel via member injection.
@SuppressWarnings("java:S6813")
class ClaudeApiImpl implements AiApi {
    private static final Logger log = LoggerFactory.getLogger(ClaudeApiImpl.class);
    // pause_turn means Anthropic's server-side MCP loop hit its internal iteration cap mid-turn; the
    // text assembled so far is coherent, so we accept it rather than failing (see assemble()).
    private static final Set<String> GOOD_STOP_REASONS = Set.of("end_turn", "pause_turn");
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private static final String MCP_BETA = "mcp-client-2025-11-20";
    private static final String CONTEXT7_MCP_URL = "https://mcp.context7.com/mcp";
    private static final String CONTEXT7_NAME = "context7";
    private static final String CONTEXT7_KEY_PROPERTY = "context7.api.key";
    private static final Integer MAX_TOKENS = 32768;
    private static final Gson gson = new Gson();
    private static final URI endpoint = URI.create("https://api.anthropic.com/v1/messages");
    private final String model;
    private final Effort effort;
    // When true (and context7.api.key is set), this binding attaches the Context7 documentation MCP
    // server so the model can look up current library docs. Off for the grammar binding.
    private final boolean context7Enabled;
    // Package-private for member injection by Guice and direct assignment in unit tests.
    @Inject
    ConfigModel configModel;

    ClaudeApiImpl(String model, Effort effort, boolean context7Enabled) {
        this.model = model;
        this.effort = effort;
        this.context7Enabled = context7Enabled;
    }

    @Override
    public AiResponse send(List<ConversationTurn> turns, Consumer<String> onTextDelta) {
        log.info("Sending question: {}", turns);
        var apiKey = configModel.getProperty("claude.api.key");
        var context7Key = context7Key();
        try (var client = HttpClient.newHttpClient()) {
            var body = buildRequestBody(turns, context7Key);
            var json = gson.toJson(body);
            if (log.isTraceEnabled()) {
                log.trace("Request body: {}", context7Key != null ? json.replace(context7Key, "***") : json);
            }
            var request = buildHttpRequest(apiKey, json, body.mcp_servers() != null);
            var response = client.send(request, HttpResponse.BodyHandlers.ofLines());
            try (var lines = response.body()) {
                if (response.statusCode() == 200) {
                    return assemble(lines, onTextDelta);
                }
                var errorBody = SseParser.joinLines(lines);
                log.error("Claude API error status {}: {}", response.statusCode(), errorBody);
                throw new AiApiException(errorBody);
            }
        } catch (IOException | UncheckedIOException e) {
            throw new AiApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiApiException(e);
        }
    }

    AiResponse assemble(Stream<String> lines, Consumer<String> onTextDelta) {
        var state = new StreamState();
        SseParser.forEachEvent(lines, sseEvent -> applyEvent(state, sseEvent, onTextDelta));
        if (!GOOD_STOP_REASONS.contains(state.stopReason)) {
            throw new AiApiException(String.format("Wrong stop reason in response: %s", state.stopReason));
        }
        if ("pause_turn".equals(state.stopReason)) {
            log.warn("Claude MCP turn paused (pause_turn); returning the partial answer for model {}", model);
        }
        Integer totalTokens = state.inputTokens != null && state.outputTokens != null
                ? state.inputTokens + state.outputTokens : null;
        return new AiResponse(state.text.toString(), state.responseId, model,
                effort != null ? effort.name() : null,
                state.stopReason, state.inputTokens, state.outputTokens, totalTokens, List.copyOf(state.toolCalls));
    }

    // S6916 ("use a pattern-match guard") is a false positive on switch cases with constant
    // (String) labels: guards are only valid on type-pattern case labels per JLS 14.11.1,
    // so the suggested rewrite wouldn't compile. Confirmed rule bug: SONARJAVA-4962.
    @SuppressWarnings("java:S6916")
    private void applyEvent(StreamState state, SseParser.SseEvent sseEvent, Consumer<String> onTextDelta) {
        var event = gson.fromJson(sseEvent.data(), StreamEvent.class);
        var type = event.type() != null ? event.type() : sseEvent.event();
        switch (type) {
            case "message_start" -> applyMessageStart(state, event);
            case "content_block_start" -> applyContentBlockStart(state, event, onTextDelta);
            case "content_block_delta" -> applyContentBlockDelta(state, event, onTextDelta);
            case "content_block_stop" -> applyContentBlockStop(state, event);
            case "message_delta" -> applyMessageDelta(state, event);
            case "error" -> throw new AiApiException(sseEvent.data());
            default -> { // message_stop, ping
            }
        }
    }

    private static void applyMessageStart(StreamState state, StreamEvent event) {
        if (event.message() != null) {
            state.responseId = event.message().id();
            if (event.message().usage() != null) {
                state.inputTokens = event.message().usage().input_tokens();
            }
        }
    }

    // Opens a pending tool call for each mcp_tool_use block; its input arrives via input_json_delta.
    // A tool-using turn interleaves multiple "text" blocks around the tool blocks; their deltas would
    // otherwise be concatenated with no gap (e.g. "...term.## Verdict:"), so a paragraph break is
    // inserted whenever a new text block opens after text has already been accumulated.
    private static void applyContentBlockStart(StreamState state, StreamEvent event, Consumer<String> onTextDelta) {
        if (event.content_block() == null) {
            return;
        }
        var blockType = event.content_block().type();
        if (event.index() != null && "mcp_tool_use".equals(blockType)) {
            state.pendingToolCalls.put(event.index(),
                    new ToolCallAccumulator(event.content_block().server_name(), event.content_block().name()));
        } else if ("text".equals(blockType) && !state.text.isEmpty()) {
            state.text.append("\n\n");
            onTextDelta.accept("\n\n");
        }
    }

    private static void applyContentBlockDelta(StreamState state, StreamEvent event, Consumer<String> onTextDelta) {
        if (event.delta() == null) {
            return;
        }
        if ("text_delta".equals(event.delta().type()) && event.delta().text() != null) {
            state.text.append(event.delta().text());
            onTextDelta.accept(event.delta().text());
        } else if ("input_json_delta".equals(event.delta().type()) && event.delta().partial_json() != null
                && event.index() != null) {
            var accumulator = state.pendingToolCalls.get(event.index());
            if (accumulator != null) {
                accumulator.input.append(event.delta().partial_json());
            }
        }
    }

    // Finalizes the tool call opened at this index (mcp_tool_result and text blocks have no pending entry).
    private static void applyContentBlockStop(StreamState state, StreamEvent event) {
        if (event.index() == null) {
            return;
        }
        var accumulator = state.pendingToolCalls.remove(event.index());
        if (accumulator != null) {
            state.toolCalls.add(accumulator.format());
        }
    }

    private static void applyMessageDelta(StreamState state, StreamEvent event) {
        if (event.delta() != null && event.delta().stop_reason() != null) {
            state.stopReason = event.delta().stop_reason();
        }
        if (event.usage() != null && event.usage().output_tokens() != null) {
            state.outputTokens = event.usage().output_tokens();
        }
    }

    private static class StreamState {
        final StringBuilder text = new StringBuilder();
        final List<String> toolCalls = new ArrayList<>();
        final Map<Integer, ToolCallAccumulator> pendingToolCalls = new HashMap<>();
        String responseId;
        String stopReason;
        Integer inputTokens;
        Integer outputTokens;
    }

    private static final class ToolCallAccumulator {
        private final String server;
        private final String name;
        private final StringBuilder input = new StringBuilder();

        ToolCallAccumulator(String server, String name) {
            this.server = server;
            this.name = name;
        }

        String format() {
            return ToolCalls.line(server, name, input.toString());
        }
    }

    RequestBody buildRequestBody(List<ConversationTurn> turns, String context7Key) {
        var outputConfig = effort != null ? new OutputConfig(effort) : null;
        var messages = turns.stream().map(turn -> new Message(role(turn.speaker()), turn.content())).toList();
        List<McpServer> mcpServers = null;
        List<McpToolset> tools = null;
        if (context7Key != null) {
            mcpServers = List.of(new McpServer("url", CONTEXT7_NAME, CONTEXT7_MCP_URL, context7Key));
            tools = List.of(new McpToolset("mcp_toolset", CONTEXT7_NAME));
        }
        return new RequestBody(model, MAX_TOKENS, messages, outputConfig, true, mcpServers, tools);
    }

    // mcpEnabled adds the beta header required by the server-side MCP connector.
    HttpRequest buildHttpRequest(String apiKey, String json, boolean mcpEnabled) {
        var requestBuilder = HttpRequest.newBuilder()
                .uri(endpoint)
                .header("x-api-key", apiKey)
                .header("anthropic-version", ANTHROPIC_VERSION)
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .timeout(Duration.ofMinutes(1));
        if (mcpEnabled) {
            requestBuilder.header("anthropic-beta", MCP_BETA);
        }
        return requestBuilder.build();
    }

    String context7Key() {
        if (!context7Enabled) {
            return null;
        }
        var key = configModel.getProperty(CONTEXT7_KEY_PROPERTY);
        return key != null && !key.isBlank() ? key : null;
    }

    private static String role(ConversationTurn.Speaker speaker) {
        return switch (speaker) {
            case USER -> "user";
            case MODEL -> "assistant";
        };
    }
}

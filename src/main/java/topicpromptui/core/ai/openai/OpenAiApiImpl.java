package topicpromptui.core.ai.openai;

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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

// OpenAiModule builds this instance manually (new OpenAiApiImpl(model, effort, context7Enabled)) and binds it via
// toInstance(...) so the hardcoded model/effort constants stay per-binding; Guice therefore never
// calls this constructor and can only supply configModel via member injection.
@SuppressWarnings("java:S6813")
class OpenAiApiImpl implements AiApi {
    private static final Logger log = LoggerFactory.getLogger(OpenAiApiImpl.class);
    private static final String CONTEXT7_MCP_URL = "https://mcp.context7.com/mcp";
    private static final String CONTEXT7_NAME = "context7";
    private static final String CONTEXT7_KEY_PROPERTY = "context7.api.key";
    // Also reads URLs appearing in the conversation, so there is no separate web-fetch tool to
    // declare here (unlike Claude and Gemini).
    private static final Tool WEB_SEARCH_TOOL = new Tool("web_search", null, null, null, null);
    private static final Gson gson = new Gson();
    private static final URI endpoint = URI.create("https://api.openai.com/v1/responses");
    private final String model;
    private final ReasoningEffort effort;
    // Off for the grammar binding: a grammar check is single-shot and never needs a lookup.
    private final boolean toolsEnabled;
    // Package-private for member injection by Guice and direct assignment in unit tests.
    @Inject
    ConfigModel configModel;

    OpenAiApiImpl(String model, ReasoningEffort effort, boolean toolsEnabled) {
        this.model = model;
        this.effort = effort;
        this.toolsEnabled = toolsEnabled;
    }

    @Override
    public AiResponse send(String systemPrompt, List<ConversationTurn> turns, Consumer<String> onTextDelta) {
        log.info("Sending question: {}", turns);
        var token = configModel.getProperty("openai.token");
        var context7Key = context7Key();
        var body = buildRequestBody(systemPrompt, turns, context7Key);
        var json = gson.toJson(body);
        if (log.isTraceEnabled()) {
            log.trace("Request body: {}", context7Key != null ? json.replace(context7Key, "***") : json);
        }
        var request = buildHttpRequest(token, json);
        try (var client = HttpClient.newHttpClient()) {
            var response = client.send(request, HttpResponse.BodyHandlers.ofLines());
            try (var lines = response.body()) {
                if (response.statusCode() == 200) {
                    return assemble(lines, onTextDelta);
                }
                var errorBody = SseParser.joinLines(lines);
                log.error("GPT API error status {}: {}", response.statusCode(), errorBody);
                throw new AiApiException(errorBody);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage(), e);
            throw new AiApiException(e);
        } catch (AiApiException e) {
            throw e;
        } catch (Exception e) { // incl. UncheckedIOException from a mid-stream disconnect
            log.error(e.getMessage(), e);
            throw new AiApiException(e);
        }
    }

    // S6916 ("use a pattern-match guard") is a false positive on switch cases with constant
    // (String) labels: guards are only valid on type-pattern case labels per JLS 14.11.1,
    // so the suggested rewrite wouldn't compile. Confirmed rule bug: SONARJAVA-4962.
    @SuppressWarnings("java:S6916")
    AiResponse assemble(Stream<String> lines, Consumer<String> onTextDelta) {
        ResponseBody[] finalBody = new ResponseBody[1];
        SseParser.forEachEvent(lines, sseEvent -> {
            var event = gson.fromJson(sseEvent.data(), StreamEvent.class);
            var type = event.type() != null ? event.type() : sseEvent.event();
            if (type == null) {
                return;
            }
            switch (type) {
                case "response.output_text.delta" -> {
                    if (event.delta() != null) {
                        onTextDelta.accept(event.delta());
                    }
                }
                case "response.completed" -> finalBody[0] = event.response();
                case "response.failed", "response.incomplete", "error" ->
                        throw new AiApiException(sseEvent.data());
                default -> { // response.created, response.output_item.*, etc.
                }
            }
        });
        if (finalBody[0] == null) {
            throw new AiApiException("Stream ended without a response.completed event");
        }
        return parseResponse(finalBody[0]);
    }

    AiResponse parseResponse(ResponseBody responseBody) {
        var outputs = responseBody.output();
        // With the MCP tool enabled the output array also carries mcp_list_tools / mcp_call / reasoning
        // items; the assistant's answer is the single "message" output. Select it rather than assuming
        // the array holds exactly one item.
        var messageOutputs = outputs.stream()
                .filter(output -> "message".equalsIgnoreCase(output.type()))
                .toList();
        if (messageOutputs.isEmpty()) {
            throw new AiApiException("No message output in response: " + outputs);
        }
        if (messageOutputs.size() > 1) {
            throw new AiApiException("Multiple message outputs in response: " + outputs);
        }
        var message = messageOutputs.getFirst();
        if (!"completed".equalsIgnoreCase(message.status())) {
            throw new AiApiException("Message output not completed in response: " + outputs);
        }
        var contents = message.content();
        if (contents.size() > 1) {
            throw new AiApiException("Multiple contents in output: " + contents);
        }
        var toolCalls = outputs.stream()
                .map(OpenAiApiImpl::toolCallLine)
                .filter(Objects::nonNull)
                .toList();
        var usage = responseBody.usage();
        return new AiResponse(contents.getFirst().text(), responseBody.id(), model,
                effort != null ? effort.name() : null,
                message.status(),
                usage != null ? usage.input_tokens() : null,
                usage != null ? usage.output_tokens() : null,
                usage != null ? usage.total_tokens() : null, toolCalls);
    }

    // A web_search_call carries no server_label/name/arguments and describes itself in "action"
    // instead, so without its own branch it would render as "(unknown)".
    private static String toolCallLine(ResponseBody.Outputs output) {
        if ("mcp_call".equalsIgnoreCase(output.type())) {
            return ToolCalls.line(output.server_label(), output.name(), output.arguments());
        }
        if ("web_search_call".equalsIgnoreCase(output.type())) {
            return ToolCalls.line("openai", "web_search", webSearchDetail(output.action()));
        }
        return null;
    }

    private static String webSearchDetail(ResponseBody.Action action) {
        if (action == null) {
            return null;
        }
        return action.query() != null ? action.query() : action.url();
    }

    RequestBody buildRequestBody(String systemPrompt, List<ConversationTurn> turns, String context7Key) {
        var reasoning = effort != null ? new Reasoning(effort) : null;
        var input = turns.stream().map(turn -> new InputItem(role(turn.speaker()), turn.content())).toList();
        var tools = new ArrayList<Tool>();
        if (toolsEnabled) {
            tools.add(WEB_SEARCH_TOOL);
        }
        if (context7Key != null) {
            tools.add(new Tool("mcp", CONTEXT7_NAME, CONTEXT7_MCP_URL,
                    Map.of("Authorization", "Bearer " + context7Key), "never"));
        }
        return new RequestBody(model, systemPrompt, input, reasoning, true, tools.isEmpty() ? null : List.copyOf(tools));
    }

    HttpRequest buildHttpRequest(String token, String json) {
        return HttpRequest.newBuilder()
                .uri(endpoint)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                // Bounds time-to-headers only (the body is read as a line stream); a server-side
                // web search or MCP call can delay the first byte well past a minute.
                .timeout(Duration.ofMinutes(3))
                .build();
    }

    String context7Key() {
        if (!toolsEnabled) {
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

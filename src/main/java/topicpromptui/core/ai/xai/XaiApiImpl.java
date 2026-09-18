package topicpromptui.core.ai.xai;

import com.google.gson.Gson;
import topicpromptui.core.ai.AiApi;
import topicpromptui.core.ai.AiApiException;
import topicpromptui.core.ai.AiResponse;
import topicpromptui.core.ai.Citation;
import topicpromptui.core.ai.Citations;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

// XaiModule builds this instance manually (new XaiApiImpl(model, effort, toolsEnabled)) and binds it via
// toInstance(...) so the hardcoded model/effort constants stay per-binding; Guice therefore never
// calls this constructor and can only supply configModel via member injection.
@SuppressWarnings("java:S6813")
class XaiApiImpl implements AiApi {
    private static final Logger log = LoggerFactory.getLogger(XaiApiImpl.class);
    private static final String CONTEXT7_MCP_URL = "https://mcp.context7.com/mcp";
    private static final String CONTEXT7_NAME = "context7";
    private static final String CONTEXT7_KEY_PROPERTY = "context7.api.key";
    // Also reads URLs appearing in the conversation, so there is no separate web-fetch tool to
    // declare here (unlike Claude and Gemini).
    private static final Tool WEB_SEARCH_TOOL = new Tool("web_search", null, null, null, null);
    // Grok leaks its raw end-of-sequence token into the answer text - seen both as a trailing
    // response.output_text.delta of its own and inside the response.completed message - so it is
    // stripped from the deltas and from the assembled text rather than reaching the UI or storage.
    private static final String EOS_TOKEN = "<|eos|>";
    private static final Gson gson = new Gson();
    // xAI's Responses API is wire-compatible with OpenAI's, down to the SSE event names, so this
    // impl mirrors OpenAiApiImpl; see parseResponse for the one behavioural difference.
    private static final URI endpoint = URI.create("https://api.x.ai/v1/responses");
    private final String model;
    private final ReasoningEffort effort;
    private final boolean toolsEnabled;
    // Package-private for member injection by Guice and direct assignment in unit tests.
    @Inject
    ConfigModel configModel;

    XaiApiImpl(String model, ReasoningEffort effort, boolean toolsEnabled) {
        this.model = model;
        this.effort = effort;
        this.toolsEnabled = toolsEnabled;
    }

    @Override
    public AiResponse send(String systemPrompt, List<ConversationTurn> turns, Consumer<String> onTextDelta) {
        log.info("Sending question: {}", turns);
        var token = configModel.getProperty("xai.key");
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
                log.error("xAI API error status {}: {}", response.statusCode(), errorBody);
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
                    var delta = stripEosToken(event.delta());
                    if (delta != null && !delta.isEmpty()) {
                        onTextDelta.accept(delta);
                    }
                }
                case "response.completed" -> finalBody[0] = event.response();
                case "response.failed", "response.incomplete", "error" ->
                        throw new AiApiException(sseEvent.data());
                // Grok also streams response.reasoning_summary_text.delta with its thinking summary;
                // that is not answer text and must not reach onTextDelta.
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
        // With tools enabled the output array also carries mcp_list_tools / mcp_call / web_search_call /
        // reasoning items; select the assistant "message" outputs rather than assuming a single item.
        // Unlike OpenAI, Grok can emit *several* message outputs - it interleaves commentary between
        // tool calls - so they are concatenated instead of rejected. The separator is "" because the
        // same text arrived as a plain run of response.output_text.delta fragments, and callers
        // (ProgressThrottler, the streaming IT) compare text() against those deltas joined with "".
        var messageOutputs = outputs.stream()
                .filter(output -> "message".equalsIgnoreCase(output.type()))
                .toList();
        if (messageOutputs.isEmpty()) {
            throw new AiApiException("No message output in response: " + outputs);
        }
        var notCompleted = messageOutputs.stream()
                .filter(message -> !"completed".equalsIgnoreCase(message.status()))
                .toList();
        if (!notCompleted.isEmpty()) {
            throw new AiApiException("Message output not completed in response: " + outputs);
        }
        var text = stripEosToken(messageOutputs.stream()
                .flatMap(message -> message.content().stream())
                .map(ResponseBody.Content::text)
                .collect(Collectors.joining()));
        var toolCalls = outputs.stream()
                .map(XaiApiImpl::toolCallLine)
                .filter(Objects::nonNull)
                .toList();
        var usage = responseBody.usage();
        return new AiResponse(text, responseBody.id(), model,
                effort != null ? effort.name() : null,
                messageOutputs.getLast().status(),
                usage != null ? usage.input_tokens() : null,
                usage != null ? usage.output_tokens() : null,
                usage != null ? usage.total_tokens() : null, toolCalls, citations(messageOutputs));
    }

    private static List<Citation> citations(List<ResponseBody.Outputs> messageOutputs) {
        return Citations.dedup(messageOutputs.stream()
                .flatMap(message -> message.content().stream())
                .filter(content -> content.annotations() != null)
                .flatMap(content -> content.annotations().stream())
                .filter(annotation -> "url_citation".equalsIgnoreCase(annotation.type()))
                .map(annotation -> new Citation(annotation.url(), annotation.title()))
                .toList());
    }

    private static String stripEosToken(String text) {
        return text != null ? text.replace(EOS_TOKEN, "") : null;
    }

    // A web_search_call carries no server_label/name/arguments and describes itself in "action"
    // instead, so without its own branch it would render as "(unknown)".
    private static String toolCallLine(ResponseBody.Outputs output) {
        if ("mcp_call".equalsIgnoreCase(output.type())) {
            return ToolCalls.line(output.server_label(), output.name(), output.arguments());
        }
        if ("web_search_call".equalsIgnoreCase(output.type())) {
            return ToolCalls.line("xai", "web_search", webSearchDetail(output.action()));
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

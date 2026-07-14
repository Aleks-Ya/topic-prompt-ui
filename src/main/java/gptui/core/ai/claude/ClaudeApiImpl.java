package gptui.core.ai.claude;

import com.google.gson.Gson;
import gptui.core.ai.AiApi;
import gptui.core.ai.AiApiException;
import gptui.core.ai.AiResponse;
import gptui.core.ai.ConversationTurn;
import gptui.core.ai.SseParser;
import gptui.core.config.ConfigModel;
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
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

// ClaudeModule builds this instance manually (new ClaudeApiImpl(model, effort)) and binds it via
// toInstance(...) so the hardcoded model/effort constants stay per-binding; Guice therefore never
// calls this constructor and can only supply configModel via member injection.
@SuppressWarnings("java:S6813")
class ClaudeApiImpl implements AiApi {
    private static final Logger log = LoggerFactory.getLogger(ClaudeApiImpl.class);
    private static final String GOOD_STOP_REASON = "end_turn";
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private static final Integer MAX_TOKENS = 8192;
    private static final Gson gson = new Gson();
    private static final URI endpoint = URI.create("https://api.anthropic.com/v1/messages");
    private final String model;
    private final Effort effort;
    @Inject
    private ConfigModel configModel;

    ClaudeApiImpl(String model, Effort effort) {
        this.model = model;
        this.effort = effort;
    }

    @Override
    public AiResponse send(List<ConversationTurn> turns, Consumer<String> onTextDelta) {
        log.info("Sending question: {}", turns);
        var apiKey = configModel.getProperty("claude.api.key");
        try (var client = HttpClient.newHttpClient()) {
            var outputConfig = effort != null ? new OutputConfig(effort) : null;
            var messages = turns.stream().map(turn -> new Message(role(turn.speaker()), turn.content())).toList();
            var body = new RequestBody(model, MAX_TOKENS, messages, outputConfig, true);
            var json = gson.toJson(body);
            log.trace("Request body: {}", json);
            var request = HttpRequest.newBuilder()
                    .uri(endpoint)
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofMinutes(1))
                    .build();
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
        if (!GOOD_STOP_REASON.equals(state.stopReason)) {
            throw new AiApiException(String.format("Wrong stop reason in response: %s", state.stopReason));
        }
        Integer totalTokens = state.inputTokens != null && state.outputTokens != null
                ? state.inputTokens + state.outputTokens : null;
        return new AiResponse(state.text.toString(), state.responseId, model,
                effort != null ? effort.name() : null,
                state.stopReason, state.inputTokens, state.outputTokens, totalTokens);
    }

    // S6916 ("use a pattern-match guard") is a false positive on switch cases with constant
    // (String) labels: guards are only valid on type-pattern case labels per JLS 14.11.1,
    // so the suggested rewrite wouldn't compile. Confirmed rule bug: SONARJAVA-4962.
    @SuppressWarnings("java:S6916")
    private void applyEvent(StreamState state, SseParser.SseEvent sseEvent, Consumer<String> onTextDelta) {
        var event = gson.fromJson(sseEvent.data(), StreamEvent.class);
        var type = event.type() != null ? event.type() : sseEvent.event();
        switch (type) {
            case "message_start" -> {
                if (event.message() != null) {
                    state.responseId = event.message().id();
                    if (event.message().usage() != null) {
                        state.inputTokens = event.message().usage().input_tokens();
                    }
                }
            }
            case "content_block_delta" -> {
                if (event.delta() != null && "text_delta".equals(event.delta().type())
                        && event.delta().text() != null) {
                    state.text.append(event.delta().text());
                    onTextDelta.accept(event.delta().text());
                }
            }
            case "message_delta" -> {
                if (event.delta() != null && event.delta().stop_reason() != null) {
                    state.stopReason = event.delta().stop_reason();
                }
                if (event.usage() != null && event.usage().output_tokens() != null) {
                    state.outputTokens = event.usage().output_tokens();
                }
            }
            case "error" -> throw new AiApiException(sseEvent.data());
            default -> { // message_stop, content_block_start/stop, ping
            }
        }
    }

    private static class StreamState {
        final StringBuilder text = new StringBuilder();
        String responseId;
        String stopReason;
        Integer inputTokens;
        Integer outputTokens;
    }

    private static String role(ConversationTurn.Speaker speaker) {
        return switch (speaker) {
            case USER -> "user";
            case MODEL -> "assistant";
        };
    }
}

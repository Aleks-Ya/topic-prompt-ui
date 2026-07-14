package gptui.core.ai.gcp;

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

import static gptui.core.ai.gcp.ResponseBody.FinishReason.STOP;

// GcpModule builds this instance manually (new GcpApiImpl(model, effort)) and binds it via
// toInstance(...) so the hardcoded model/effort constants stay per-binding; Guice therefore never
// calls this constructor and can only supply configModel via member injection.
@SuppressWarnings("java:S6813")
class GcpApiImpl implements AiApi {
    private static final Logger log = LoggerFactory.getLogger(GcpApiImpl.class);
    private static final Gson gson = new Gson();
    private static final String ENDPOINT_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:streamGenerateContent?alt=sse";
    private final String model;
    private final URI endpoint;
    private final ThinkingLevel effort;
    @Inject
    private ConfigModel configModel;

    GcpApiImpl(String model, ThinkingLevel effort) {
        this.model = model;
        this.endpoint = URI.create(String.format(ENDPOINT_TEMPLATE, model));
        this.effort = effort;
    }

    @Override
    public AiResponse send(List<ConversationTurn> turns, Consumer<String> onTextDelta) {
        log.info("Sending question: {}", turns);
        var apiKey = configModel.getProperty("gcp.api.key");
        try (var client = HttpClient.newHttpClient()) {
            var thinkingConfig = effort != null ? new ThinkingConfig(effort) : null;
            var contents = turns.stream()
                    .map(turn -> new Content(List.of(new Part(turn.content())), role(turn.speaker())))
                    .toList();
            var body = new RequestBody(contents, new GenerationConfig(1, thinkingConfig));
            var json = gson.toJson(body);
            log.trace("Request body: {}", json);
            var request = HttpRequest.newBuilder()
                    .uri(endpoint)
                    .header("X-goog-api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofMinutes(1))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofLines());
            try (var lines = response.body()) {
                if (response.statusCode() == 200) {
                    return assemble(lines, onTextDelta);
                }
                var errorBody = SseParser.joinLines(lines);
                log.error("GCP API error status {}: {}", response.statusCode(), errorBody);
                throw new AiApiException(errorBody);
            }
        } catch (IOException | UncheckedIOException e) {
            log.error(e.getMessage(), e);
            throw new AiApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage(), e);
            throw new AiApiException(e);
        }
    }

    AiResponse assemble(Stream<String> lines, Consumer<String> onTextDelta) {
        var state = new StreamState();
        SseParser.forEachEvent(lines, sseEvent ->
                applyFragment(state, gson.fromJson(sseEvent.data(), ResponseBody.class), onTextDelta));
        if (state.finishReason != STOP) {
            throw new AiApiException(String.format("Wrong finish reason in candidate: %s", state.finishReason));
        }
        return new AiResponse(state.text.toString(), state.responseId,
                model, effort != null ? effort.name() : null, state.finishReason.name(),
                state.usage != null ? state.usage.promptTokenCount() : null,
                state.usage != null ? state.usage.candidatesTokenCount() : null,
                state.usage != null ? state.usage.totalTokenCount() : null);
    }

    private static void applyFragment(StreamState state, ResponseBody fragment, Consumer<String> onTextDelta) {
        if (fragment.responseId() != null) {
            state.responseId = fragment.responseId();
        }
        if (fragment.usageMetadata() != null) {
            state.usage = fragment.usageMetadata();
        }
        if (fragment.candidates() == null || fragment.candidates().isEmpty()) {
            return;
        }
        var candidate = fragment.candidates().getFirst();
        if (candidate.finishReason() != null) {
            state.finishReason = candidate.finishReason();
        }
        if (candidate.content() != null && candidate.content().parts() != null) {
            for (var part : candidate.content().parts()) {
                if (part.text() != null) {
                    state.text.append(part.text());
                    onTextDelta.accept(part.text());
                }
            }
        }
    }

    private static class StreamState {
        final StringBuilder text = new StringBuilder();
        String responseId;
        ResponseBody.FinishReason finishReason;
        ResponseBody.UsageMetadata usage;
    }

    private static String role(ConversationTurn.Speaker speaker) {
        return switch (speaker) {
            case USER -> "user";
            case MODEL -> "model";
        };
    }
}

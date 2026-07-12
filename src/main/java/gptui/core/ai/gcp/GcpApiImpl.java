package gptui.core.ai.gcp;

import com.google.gson.Gson;
import gptui.core.ai.AiApi;
import gptui.core.ai.AiApiException;
import gptui.core.ai.AiResponse;
import gptui.core.ai.ConversationTurn;
import gptui.ui.model.config.ConfigModel;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import static gptui.core.ai.gcp.ResponseBody.FinishReason.STOP;

class GcpApiImpl implements AiApi {
    private static final Logger log = LoggerFactory.getLogger(GcpApiImpl.class);
    private static final Gson gson = new Gson();
    private static final String ENDPOINT_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";
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
    public AiResponse send(List<ConversationTurn> turns) {
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
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                var responseBody = gson.fromJson(response.body(), ResponseBody.class);
                return parseResponse(responseBody);
            } else {
                log.error("GCP API error status {}: {}", response.statusCode(), response.body());
                throw new AiApiException(response.body());
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new AiApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage(), e);
            throw new AiApiException(e);
        }
    }

    AiResponse parseResponse(ResponseBody responseBody) {
        var candidate = responseBody.candidates().getFirst();
        if (candidate.finishReason() != STOP) {
            var message = String.format("Wrong finish reason in candidate: %s", candidate);
            throw new AiApiException(message);
        }
        var usage = responseBody.usageMetadata();
        return new AiResponse(candidate.content().parts().getFirst().text(), responseBody.responseId(),
                model, effort != null ? effort.name() : null, candidate.finishReason().name(),
                usage != null ? usage.promptTokenCount() : null,
                usage != null ? usage.candidatesTokenCount() : null,
                usage != null ? usage.totalTokenCount() : null);
    }

    private static String role(ConversationTurn.Speaker speaker) {
        return switch (speaker) {
            case USER -> "user";
            case MODEL -> "model";
        };
    }
}

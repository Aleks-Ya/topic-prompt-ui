package gptui.core.ai.gcp;

import com.google.gson.Gson;
import gptui.core.ai.AiApi;
import gptui.ui.model.config.ConfigModel;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final URI endpoint;
    @Inject
    private ConfigModel configModel;

    GcpApiImpl(String model) {
        this.endpoint = URI.create(String.format(ENDPOINT_TEMPLATE, model));
    }

    @Override
    public String send(String content) {
        log.info("Sending question: {}", content);
        var apiKey = configModel.getProperty("gcp.api.key");
        try (var client = HttpClient.newHttpClient()) {
            var body = new RequestBody(List.of(new Content(List.of(new Part(content)), "user")),
                    new GenerationConfig(1));
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
                var candidate = responseBody.candidates().getFirst();
                if (candidate.finishReason() != STOP) {
                    var message = String.format("Wrong finish reason in candidate: %s", candidate);
                    throw new RuntimeException(message);
                }
                return candidate.content().parts().getFirst().text();
            } else {
                log.error("GCP API error status {}: {}", response.statusCode(), response.body());
                throw new RuntimeException(response.body());
            }
        } catch (RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}

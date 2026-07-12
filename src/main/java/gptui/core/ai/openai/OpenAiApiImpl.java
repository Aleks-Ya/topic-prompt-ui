package gptui.core.ai.openai;

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

class OpenAiApiImpl implements AiApi {
    private static final Logger log = LoggerFactory.getLogger(OpenAiApiImpl.class);
    private static final Gson gson = new Gson();
    private static final URI endpoint = URI.create("https://api.openai.com/v1/responses");
    private final String model;
    private final ReasoningEffort effort;
    @Inject
    private ConfigModel configModel;

    OpenAiApiImpl(String model, ReasoningEffort effort) {
        this.model = model;
        this.effort = effort;
    }

    @Override
    public String send(String content) {
        log.info("Sending question: {}", content);
        var token = configModel.getProperty("openai.token");
        var reasoning = effort != null ? new Reasoning(effort) : null;
        var body = new RequestBody(model, content, reasoning);
        var json = gson.toJson(body);
        log.trace("Request body: {}", json);
        HttpResponse<String> response;
        var request = HttpRequest.newBuilder()
                .uri(endpoint)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .timeout(Duration.ofMinutes(1))
                .build();
        try (var client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        if (response.statusCode() == 200) {
            var responseBody = gson.fromJson(response.body(), ResponseBody.class);
            var outputs = responseBody.output();
            var completedOutputs = outputs.stream()
                    .filter(output -> "completed".equalsIgnoreCase(output.status()))
                    .toList();
            if (completedOutputs.size() > 1) {
                throw new RuntimeException("Multiple outputs in response: " + outputs);
            }
            var contents = completedOutputs.getFirst().content();
            if (contents.size() > 1) {
                throw new RuntimeException("Multiple contents in output: " + contents);
            }
            return contents.getFirst().text();
        } else {
            log.error("GPT API error status {}: {}", response.statusCode(), response.body());
            throw new RuntimeException(response.body());
        }
    }

}

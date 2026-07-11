package gptui.core.ai.claude;

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
import java.util.Set;
import java.util.stream.Collectors;

class ClaudeApiImpl implements AiApi {
    private static final Logger log = LoggerFactory.getLogger(ClaudeApiImpl.class);
    private static final Set<String> BAD_STOP_REASONS = Set.of("max_tokens", "refusal");
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private static final Integer MAX_TOKENS = 8192;
    private static final Gson gson = new Gson();
    private static final URI endpoint = URI.create("https://api.anthropic.com/v1/messages");
    private final String model;
    @Inject
    private ConfigModel configModel;

    ClaudeApiImpl(String model) {
        this.model = model;
    }

    @Override
    public String send(String content) {
        log.info("Sending question: {}", content);
        var apiKey = configModel.getProperty("claude.api.key");
        try (var client = HttpClient.newHttpClient()) {
            var body = new RequestBody(model, MAX_TOKENS, List.of(new Message("user", content)));
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
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                var responseBody = gson.fromJson(response.body(), ResponseBody.class);
                if (BAD_STOP_REASONS.contains(responseBody.stop_reason())) {
                    var message = String.format("Wrong stop reason in response: %s", responseBody);
                    throw new RuntimeException(message);
                }
                return responseBody.content().stream()
                        .filter(block -> "text".equals(block.type()))
                        .map(ResponseBody.ContentBlock::text)
                        .collect(Collectors.joining());
            } else {
                log.error("Claude API error status {}: {}", response.statusCode(), response.body());
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

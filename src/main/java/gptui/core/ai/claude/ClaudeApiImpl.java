package gptui.core.ai.claude;

import com.google.gson.Gson;
import gptui.core.ai.AiApi;
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
    private final Effort effort;
    @Inject
    private ConfigModel configModel;

    ClaudeApiImpl(String model, Effort effort) {
        this.model = model;
        this.effort = effort;
    }

    @Override
    public AiResponse send(List<ConversationTurn> turns) {
        log.info("Sending question: {}", turns);
        var apiKey = configModel.getProperty("claude.api.key");
        try (var client = HttpClient.newHttpClient()) {
            var outputConfig = effort != null ? new OutputConfig(effort) : null;
            var messages = turns.stream().map(turn -> new Message(role(turn.speaker()), turn.content())).toList();
            var body = new RequestBody(model, MAX_TOKENS, messages, outputConfig);
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
                var text = responseBody.content().stream()
                        .filter(block -> "text".equals(block.type()))
                        .map(ResponseBody.ContentBlock::text)
                        .collect(Collectors.joining());
                return new AiResponse(text, responseBody.id());
            } else {
                log.error("Claude API error status {}: {}", response.statusCode(), response.body());
                throw new RuntimeException(response.body());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private static String role(ConversationTurn.Speaker speaker) {
        return switch (speaker) {
            case USER -> "user";
            case MODEL -> "assistant";
        };
    }
}

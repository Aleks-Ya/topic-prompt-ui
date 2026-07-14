package gptui.core.ai.openai;

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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

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
    public AiResponse send(List<ConversationTurn> turns, Consumer<String> onTextDelta) {
        log.info("Sending question: {}", turns);
        var token = configModel.getProperty("openai.token");
        var reasoning = effort != null ? new Reasoning(effort) : null;
        var input = turns.stream().map(turn -> new InputItem(role(turn.speaker()), turn.content())).toList();
        var body = new RequestBody(model, input, reasoning, true);
        var json = gson.toJson(body);
        log.trace("Request body: {}", json);
        var request = HttpRequest.newBuilder()
                .uri(endpoint)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .timeout(Duration.ofMinutes(1))
                .build();
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
        var completedOutputs = outputs.stream()
                .filter(output -> "completed".equalsIgnoreCase(output.status()))
                .toList();
        if (completedOutputs.isEmpty()) {
            throw new AiApiException("No completed output in response: " + outputs);
        }
        if (completedOutputs.size() > 1) {
            throw new AiApiException("Multiple outputs in response: " + outputs);
        }
        var contents = completedOutputs.getFirst().content();
        if (contents.size() > 1) {
            throw new AiApiException("Multiple contents in output: " + contents);
        }
        var usage = responseBody.usage();
        return new AiResponse(contents.getFirst().text(), responseBody.id(), model,
                effort != null ? effort.name() : null,
                completedOutputs.getFirst().status(),
                usage != null ? usage.input_tokens() : null,
                usage != null ? usage.output_tokens() : null,
                usage != null ? usage.total_tokens() : null);
    }

    private static String role(ConversationTurn.Speaker speaker) {
        return switch (speaker) {
            case USER -> "user";
            case MODEL -> "assistant";
        };
    }
}

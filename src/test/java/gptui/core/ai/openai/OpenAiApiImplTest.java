package gptui.core.ai.openai;

import com.google.gson.Gson;
import gptui.core.ai.AiApiException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenAiApiImplTest {
    private final Gson gson = new Gson();
    private final OpenAiApiImpl api = new OpenAiApiImpl("gpt-5", null);

    private static Stream<String> sse(String... eventTypeAndData) {
        var lines = new ArrayList<String>();
        for (var i = 0; i < eventTypeAndData.length; i += 2) {
            lines.add("event: " + eventTypeAndData[i]);
            lines.add("data: " + eventTypeAndData[i + 1]);
            lines.add("");
        }
        return lines.stream();
    }

    @Test
    void assembleEmitsDeltasAndParsesCompletedResponse() {
        var deltas = new ArrayList<String>();
        var response = api.assemble(sse(
                "response.created", """
                        {"type": "response.created"}""",
                "response.output_text.delta", """
                        {"type": "response.output_text.delta", "delta": "Full "}""",
                "response.output_text.delta", """
                        {"type": "response.output_text.delta", "delta": "answer"}""",
                "response.completed", """
                        {"type": "response.completed", "response": {"id": "resp_3", \
                        "output": [{"content": [{"text": "Full answer"}], "status": "completed"}], \
                        "usage": {"input_tokens": 10, "output_tokens": 20, "total_tokens": 30}}}"""
        ), deltas::add);
        assertThat(deltas).containsExactly("Full ", "answer");
        assertThat(response.text()).isEqualTo("Full answer");
        assertThat(response.responseId()).isEqualTo("resp_3");
        assertThat(response.finishReason()).isEqualTo("completed");
        assertThat(response.totalTokens()).isEqualTo(30);
    }

    @Test
    void assembleThrowsOnFailedEvent() {
        assertThatThrownBy(() -> api.assemble(sse(
                "response.failed", """
                        {"type": "response.failed", "response": {"id": "resp_4", "output": []}}"""
        ), delta -> {
        }))
                .isInstanceOf(AiApiException.class)
                .hasMessageContaining("response.failed");
    }

    @Test
    void assembleThrowsWhenStreamEndsWithoutCompletedEvent() {
        assertThatThrownBy(() -> api.assemble(sse(
                "response.output_text.delta", """
                        {"type": "response.output_text.delta", "delta": "Full "}"""
        ), delta -> {
        }))
                .isInstanceOf(AiApiException.class)
                .hasMessageContaining("without a response.completed");
    }

    @Test
    void parseResponseThrowsWhenTruncatedByTokenLimit() {
        var json = """
                {
                  "id": "resp_1",
                  "output": [
                    {"content": [{"text": "partial answ"}], "status": "incomplete"}
                  ],
                  "usage": {"input_tokens": 10, "output_tokens": 5, "total_tokens": 15}
                }
                """;
        var responseBody = gson.fromJson(json, ResponseBody.class);
        assertThatThrownBy(() -> api.parseResponse(responseBody))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No completed output");
    }

    @Test
    void parseResponseThrowsWhenMultipleCompletedOutputs() {
        var json = """
                {
                  "id": "resp_2",
                  "output": [
                    {"content": [{"text": "answer 1"}], "status": "completed"},
                    {"content": [{"text": "answer 2"}], "status": "completed"}
                  ],
                  "usage": {"input_tokens": 10, "output_tokens": 5, "total_tokens": 15}
                }
                """;
        var responseBody = gson.fromJson(json, ResponseBody.class);
        assertThatThrownBy(() -> api.parseResponse(responseBody))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Multiple outputs");
    }

    @Test
    void parseResponseReturnsTextWhenCompleted() {
        var json = """
                {
                  "id": "resp_3",
                  "output": [
                    {"content": [{"text": "Full answer"}], "status": "completed"}
                  ],
                  "usage": {"input_tokens": 10, "output_tokens": 20, "total_tokens": 30}
                }
                """;
        var responseBody = gson.fromJson(json, ResponseBody.class);
        var response = api.parseResponse(responseBody);
        assertThat(response.text()).isEqualTo("Full answer");
        assertThat(response.finishReason()).isEqualTo("completed");
        assertThat(response.totalTokens()).isEqualTo(30);
    }
}

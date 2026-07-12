package gptui.core.ai.openai;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenAiApiImplTest {
    private final Gson gson = new Gson();
    private final OpenAiApiImpl api = new OpenAiApiImpl("gpt-5", null);

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

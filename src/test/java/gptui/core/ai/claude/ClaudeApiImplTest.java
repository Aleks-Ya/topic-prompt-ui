package gptui.core.ai.claude;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaudeApiImplTest {
    private final Gson gson = new Gson();
    private final ClaudeApiImpl api = new ClaudeApiImpl("claude-opus", null);

    @Test
    void parseResponseThrowsWhenTruncatedByTokenLimit() {
        var json = """
                {
                  "id": "msg_1",
                  "content": [{"type": "text", "text": "partial"}],
                  "stop_reason": "max_tokens",
                  "usage": {"input_tokens": 10, "output_tokens": 8192}
                }
                """;
        var responseBody = gson.fromJson(json, ResponseBody.class);
        assertThatThrownBy(() -> api.parseResponse(responseBody))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Wrong stop reason");
    }

    @Test
    void parseResponseThrowsWhenRefused() {
        var json = """
                {
                  "id": "msg_2",
                  "content": [{"type": "text", "text": ""}],
                  "stop_reason": "refusal",
                  "usage": {"input_tokens": 10, "output_tokens": 1}
                }
                """;
        var responseBody = gson.fromJson(json, ResponseBody.class);
        assertThatThrownBy(() -> api.parseResponse(responseBody))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Wrong stop reason");
    }

    @Test
    void parseResponseReturnsTextWhenEndTurn() {
        var json = """
                {
                  "id": "msg_3",
                  "content": [{"type": "text", "text": "Full answer"}],
                  "stop_reason": "end_turn",
                  "usage": {"input_tokens": 10, "output_tokens": 20}
                }
                """;
        var responseBody = gson.fromJson(json, ResponseBody.class);
        var response = api.parseResponse(responseBody);
        assertThat(response.text()).isEqualTo("Full answer");
        assertThat(response.finishReason()).isEqualTo("end_turn");
        assertThat(response.totalTokens()).isEqualTo(30);
    }
}

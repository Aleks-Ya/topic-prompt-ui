package gptui.core.ai.gcp;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GcpApiImplTest {
    private final Gson gson = new Gson();
    private final GcpApiImpl api = new GcpApiImpl("gemini-pro", null);

    @Test
    void parseResponseThrowsWhenTruncatedByTokenLimit() {
        var json = """
                {
                  "candidates": [
                    {"content": {"parts": [{"text": "partial"}], "role": "model"}, "finishReason": "MAX_TOKENS"}
                  ],
                  "responseId": "resp_1",
                  "usageMetadata": {"promptTokenCount": 10, "candidatesTokenCount": 100, "totalTokenCount": 110}
                }
                """;
        var responseBody = gson.fromJson(json, ResponseBody.class);
        assertThatThrownBy(() -> api.parseResponse(responseBody))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Wrong finish reason");
    }

    @Test
    void parseResponseThrowsWhenBlockedBySafety() {
        var json = """
                {
                  "candidates": [
                    {"content": {"parts": [{"text": ""}], "role": "model"}, "finishReason": "SAFETY"}
                  ],
                  "responseId": "resp_2",
                  "usageMetadata": {"promptTokenCount": 10, "candidatesTokenCount": 0, "totalTokenCount": 10}
                }
                """;
        var responseBody = gson.fromJson(json, ResponseBody.class);
        assertThatThrownBy(() -> api.parseResponse(responseBody))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Wrong finish reason");
    }

    @Test
    void parseResponseReturnsTextWhenStop() {
        var json = """
                {
                  "candidates": [
                    {"content": {"parts": [{"text": "Full answer"}], "role": "model"}, "finishReason": "STOP"}
                  ],
                  "responseId": "resp_3",
                  "usageMetadata": {"promptTokenCount": 10, "candidatesTokenCount": 20, "totalTokenCount": 30}
                }
                """;
        var responseBody = gson.fromJson(json, ResponseBody.class);
        var response = api.parseResponse(responseBody);
        assertThat(response.text()).isEqualTo("Full answer");
        assertThat(response.finishReason()).isEqualTo("STOP");
        assertThat(response.totalTokens()).isEqualTo(30);
    }
}

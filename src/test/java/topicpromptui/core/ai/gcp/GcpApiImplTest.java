package topicpromptui.core.ai.gcp;

import topicpromptui.core.ai.AiApiException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// S5976 suggests merging the 3 assembleThrowsXXX tests below into one @ParameterizedTest. Left
// as-is for consistency with the identical one-test-per-scenario style used in the sibling
// ClaudeApiImplTest/OpenAiApiImplTest (not flagged there only because their assertions differ
// slightly per test); the descriptive method names document distinct real-world failure modes.
@SuppressWarnings("java:S5976")
class GcpApiImplTest {
    private final GcpApiImpl api = new GcpApiImpl("gemini-pro", null);

    // Gemini streamGenerateContent?alt=sse emits bare "data:" lines without "event:" names.
    private static Stream<String> sse(String... fragments) {
        var lines = new ArrayList<String>();
        for (var fragment : fragments) {
            lines.add("data: " + fragment);
            lines.add("");
        }
        return lines.stream();
    }

    @Test
    void assembleConcatenatesFragmentsAndCollectsMetadata() {
        var deltas = new ArrayList<String>();
        var response = api.assemble(sse(
                """
                        {"candidates": [{"content": {"parts": [{"text": "Full "}], "role": "model"}}]}""",
                """
                        {"candidates": [{"content": {"parts": [{"text": "ans"}, {"text": "wer"}], "role": "model"}, \
                        "finishReason": "STOP"}], "responseId": "resp_3", \
                        "usageMetadata": {"promptTokenCount": 10, "candidatesTokenCount": 20, "totalTokenCount": 30}}"""
        ), deltas::add);
        assertThat(deltas).containsExactly("Full ", "ans", "wer");
        assertThat(response.text()).isEqualTo("Full answer");
        assertThat(response.responseId()).isEqualTo("resp_3");
        assertThat(response.finishReason()).isEqualTo("STOP");
        assertThat(response.inputTokens()).isEqualTo(10);
        assertThat(response.outputTokens()).isEqualTo(20);
        assertThat(response.totalTokens()).isEqualTo(30);
    }

    @Test
    void assembleThrowsWhenTruncatedByTokenLimit() {
        var lines = sse(
                """
                        {"candidates": [{"content": {"parts": [{"text": "partial"}], "role": "model"}, \
                        "finishReason": "MAX_TOKENS"}], "responseId": "resp_1", \
                        "usageMetadata": {"promptTokenCount": 10, "candidatesTokenCount": 100, "totalTokenCount": 110}}"""
        );
        assertThatThrownBy(() -> api.assemble(lines, delta -> {
        }))
                .isInstanceOf(AiApiException.class)
                .hasMessageContaining("Wrong finish reason");
    }

    @Test
    void assembleThrowsWhenBlockedBySafety() {
        var lines = sse(
                """
                        {"candidates": [{"content": {"parts": [{"text": ""}], "role": "model"}, \
                        "finishReason": "SAFETY"}], "responseId": "resp_2", \
                        "usageMetadata": {"promptTokenCount": 10, "candidatesTokenCount": 0, "totalTokenCount": 10}}"""
        );
        assertThatThrownBy(() -> api.assemble(lines, delta -> {
        }))
                .isInstanceOf(AiApiException.class)
                .hasMessageContaining("Wrong finish reason");
    }

    @Test
    void assembleThrowsWhenStreamEndsWithoutFinishReason() {
        var lines = sse(
                """
                        {"candidates": [{"content": {"parts": [{"text": "partial"}], "role": "model"}}]}"""
        );
        assertThatThrownBy(() -> api.assemble(lines, delta -> {
        }))
                .isInstanceOf(AiApiException.class)
                .hasMessageContaining("Wrong finish reason");
    }
}

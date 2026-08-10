package topicpromptui.core.ai.gcp;

import com.google.gson.Gson;
import topicpromptui.core.ai.AiApiException;
import topicpromptui.core.ai.ConversationTurn;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static topicpromptui.core.ai.ConversationTurn.Speaker.USER;

// S5976 suggests merging the 3 assembleThrowsXXX tests below into one @ParameterizedTest. Left
// as-is for consistency with the identical one-test-per-scenario style used in the sibling
// ClaudeApiImplTest/OpenAiApiImplTest (not flagged there only because their assertions differ
// slightly per test); the descriptive method names document distinct real-world failure modes.
@SuppressWarnings("java:S5976")
class GcpApiImplTest {
    private final GcpApiImpl api = new GcpApiImpl("gemini-pro", null, false);

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
    void assembleCollectsGroundingQueriesAsToolCalls() {
        // Grounding arrives on a late fragment; finishReason stays STOP.
        var response = api.assemble(sse(
                """
                        {"candidates": [{"content": {"parts": [{"text": "Java 25 is the latest LTS."}], \
                        "role": "model"}}]}""",
                """
                        {"candidates": [{"content": {"parts": [], "role": "model"}, "finishReason": "STOP", \
                        "groundingMetadata": {"webSearchQueries": ["latest java lts", "java 25 release date"]}}], \
                        "responseId": "resp_4", \
                        "usageMetadata": {"promptTokenCount": 10, "candidatesTokenCount": 20, "totalTokenCount": 30}}"""
        ), delta -> {
        });
        assertThat(response.text()).isEqualTo("Java 25 is the latest LTS.");
        assertThat(response.finishReason()).isEqualTo("STOP");
        assertThat(response.toolCalls()).containsExactly(
                "googleSearch · web_search latest java lts",
                "googleSearch · web_search java 25 release date");
    }

    @Test
    void assembleReportsNoToolCallsWithoutGrounding() {
        var response = api.assemble(sse(
                """
                        {"candidates": [{"content": {"parts": [{"text": "Answer"}], "role": "model"}, \
                        "finishReason": "STOP"}], "responseId": "resp_5"}"""
        ), delta -> {
        });
        assertThat(response.toolCalls()).isEmpty();
    }

    @Test
    void buildRequestBodyAttachesWebToolsWhenEnabled() {
        var enabled = new GcpApiImpl("gemini-pro", ThinkingLevel.HIGH, true);
        var body = enabled.buildRequestBody("sys", List.of(new ConversationTurn(USER, "hi")));
        assertThat(body.tools()).satisfiesExactly(
                tool -> {
                    assertThat(tool.googleSearch()).isNotNull();
                    assertThat(tool.urlContext()).isNull();
                },
                tool -> {
                    assertThat(tool.googleSearch()).isNull();
                    assertThat(tool.urlContext()).isNotNull();
                });
        assertThat(body.systemInstruction().parts()).singleElement()
                .satisfies(part -> assertThat(part.text()).isEqualTo("sys"));
        assertThat(body.contents()).singleElement()
                .satisfies(content -> assertThat(content.role()).isEqualTo("user"));
    }

    @Test
    void buildRequestBodyOmitsWebToolsWhenDisabled() {
        var body = api.buildRequestBody(null, List.of(new ConversationTurn(USER, "hi")));
        assertThat(body.tools()).isNull();
        assertThat(body.systemInstruction()).isNull();
    }

    // A stray "googleSearch": null would be rejected, so the null-skipping is load-bearing.
    @Test
    void webToolsSerializeToOneEmptyObjectEach() {
        var enabled = new GcpApiImpl("gemini-pro", null, true);
        var json = new Gson().toJson(enabled.buildRequestBody(null, List.of(new ConversationTurn(USER, "hi"))));
        assertThat(json).contains("\"tools\":[{\"googleSearch\":{}},{\"urlContext\":{}}]");
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

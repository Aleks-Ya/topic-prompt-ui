package gptui.core.ai.claude;

import gptui.core.ai.AiApiException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaudeApiImplTest {
    private final ClaudeApiImpl api = new ClaudeApiImpl("claude-opus", null);

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
    void assembleConcatenatesDeltasAndCollectsMetadata() {
        var deltas = new ArrayList<String>();
        var response = api.assemble(sse(
                "message_start", """
                        {"type": "message_start", "message": {"id": "msg_1", "usage": {"input_tokens": 10}}}""",
                "content_block_start", """
                        {"type": "content_block_start", "index": 0}""",
                "content_block_delta", """
                        {"type": "content_block_delta", "delta": {"type": "text_delta", "text": "Full "}}""",
                "ping", """
                        {"type": "ping"}""",
                "content_block_delta", """
                        {"type": "content_block_delta", "delta": {"type": "text_delta", "text": "answer"}}""",
                "content_block_stop", """
                        {"type": "content_block_stop", "index": 0}""",
                "message_delta", """
                        {"type": "message_delta", "delta": {"stop_reason": "end_turn"}, "usage": {"output_tokens": 20}}""",
                "message_stop", """
                        {"type": "message_stop"}"""
        ), deltas::add);
        assertThat(deltas).containsExactly("Full ", "answer");
        assertThat(response.text()).isEqualTo("Full answer");
        assertThat(response.responseId()).isEqualTo("msg_1");
        assertThat(response.finishReason()).isEqualTo("end_turn");
        assertThat(response.inputTokens()).isEqualTo(10);
        assertThat(response.outputTokens()).isEqualTo(20);
        assertThat(response.totalTokens()).isEqualTo(30);
    }

    @Test
    void assembleThrowsWhenTruncatedByTokenLimit() {
        assertThatThrownBy(() -> api.assemble(sse(
                "message_start", """
                        {"type": "message_start", "message": {"id": "msg_1", "usage": {"input_tokens": 10}}}""",
                "content_block_delta", """
                        {"type": "content_block_delta", "delta": {"type": "text_delta", "text": "partial"}}""",
                "message_delta", """
                        {"type": "message_delta", "delta": {"stop_reason": "max_tokens"}, "usage": {"output_tokens": 8192}}"""
        ), delta -> {
        }))
                .isInstanceOf(AiApiException.class)
                .hasMessageContaining("Wrong stop reason");
    }

    @Test
    void assembleThrowsWhenRefused() {
        assertThatThrownBy(() -> api.assemble(sse(
                "message_delta", """
                        {"type": "message_delta", "delta": {"stop_reason": "refusal"}, "usage": {"output_tokens": 1}}"""
        ), delta -> {
        }))
                .isInstanceOf(AiApiException.class)
                .hasMessageContaining("Wrong stop reason");
    }

    @Test
    void assembleThrowsOnMidStreamErrorEvent() {
        assertThatThrownBy(() -> api.assemble(sse(
                "message_start", """
                        {"type": "message_start", "message": {"id": "msg_1", "usage": {"input_tokens": 10}}}""",
                "error", """
                        {"type": "error", "error": {"type": "overloaded_error", "message": "Overloaded"}}"""
        ), delta -> {
        }))
                .isInstanceOf(AiApiException.class)
                .hasMessageContaining("overloaded_error");
    }
}

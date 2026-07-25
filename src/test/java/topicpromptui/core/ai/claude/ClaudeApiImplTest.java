package topicpromptui.core.ai.claude;

import topicpromptui.core.ai.AiApiException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaudeApiImplTest {
    private final ClaudeApiImpl api = new ClaudeApiImpl("claude-opus", null, false);

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
        var lines = sse(
                "message_start", """
                        {"type": "message_start", "message": {"id": "msg_1", "usage": {"input_tokens": 10}}}""",
                "content_block_delta", """
                        {"type": "content_block_delta", "delta": {"type": "text_delta", "text": "partial"}}""",
                "message_delta", """
                        {"type": "message_delta", "delta": {"stop_reason": "max_tokens"}, "usage": {"output_tokens": 8192}}"""
        );
        assertThatThrownBy(() -> api.assemble(lines, delta -> {
        }))
                .isInstanceOf(AiApiException.class)
                .hasMessageContaining("Wrong stop reason");
    }

    @Test
    void assembleThrowsWhenRefused() {
        var lines = sse(
                "message_delta", """
                        {"type": "message_delta", "delta": {"stop_reason": "refusal"}, "usage": {"output_tokens": 1}}"""
        );
        assertThatThrownBy(() -> api.assemble(lines, delta -> {
        }))
                .isInstanceOf(AiApiException.class)
                .hasMessageContaining("Wrong stop reason");
    }

    @Test
    void assembleIgnoresMcpToolBlocksAndKeepsText() {
        // With the Context7 MCP connector, the model streams mcp_tool_use blocks (input_json_delta)
        // alongside the answer text; only text_delta must land in the assembled answer.
        var deltas = new ArrayList<String>();
        var response = api.assemble(sse(
                "message_start", """
                        {"type": "message_start", "message": {"id": "msg_2", "usage": {"input_tokens": 50}}}""",
                "content_block_start", """
                        {"type": "content_block_start", "index": 0, \
                        "content_block": {"type": "mcp_tool_use", "name": "get-library-docs"}}""",
                "content_block_delta", """
                        {"type": "content_block_delta", "index": 0, \
                        "delta": {"type": "input_json_delta", "partial_json": "{\\"library\\":"}}""",
                "content_block_stop", """
                        {"type": "content_block_stop", "index": 0}""",
                "content_block_delta", """
                        {"type": "content_block_delta", "index": 1, \
                        "delta": {"type": "text_delta", "text": "Per the docs, "}}""",
                "content_block_delta", """
                        {"type": "content_block_delta", "index": 1, \
                        "delta": {"type": "text_delta", "text": "use X."}}""",
                "message_delta", """
                        {"type": "message_delta", "delta": {"stop_reason": "end_turn"}, "usage": {"output_tokens": 30}}"""
        ), deltas::add);
        assertThat(deltas).containsExactly("Per the docs, ", "use X.");
        assertThat(response.text()).isEqualTo("Per the docs, use X.");
        assertThat(response.finishReason()).isEqualTo("end_turn");
    }

    @Test
    void assembleAcceptsPauseTurn() {
        // Anthropic's server-side MCP loop can end a turn with pause_turn; we keep the partial answer
        // rather than throwing (unlike max_tokens/refusal).
        var response = api.assemble(sse(
                "message_start", """
                        {"type": "message_start", "message": {"id": "msg_3", "usage": {"input_tokens": 5}}}""",
                "content_block_delta", """
                        {"type": "content_block_delta", "delta": {"type": "text_delta", "text": "partial so far"}}""",
                "message_delta", """
                        {"type": "message_delta", "delta": {"stop_reason": "pause_turn"}, "usage": {"output_tokens": 10}}"""
        ), delta -> {
        });
        assertThat(response.text()).isEqualTo("partial so far");
        assertThat(response.finishReason()).isEqualTo("pause_turn");
    }

    @Test
    void assembleThrowsOnMidStreamErrorEvent() {
        var lines = sse(
                "message_start", """
                        {"type": "message_start", "message": {"id": "msg_1", "usage": {"input_tokens": 10}}}""",
                "error", """
                        {"type": "error", "error": {"type": "overloaded_error", "message": "Overloaded"}}"""
        );
        assertThatThrownBy(() -> api.assemble(lines, delta -> {
        }))
                .isInstanceOf(AiApiException.class)
                .hasMessageContaining("overloaded_error");
    }
}

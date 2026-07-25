package topicpromptui.core.ai.claude;

import topicpromptui.core.ai.AiApiException;
import topicpromptui.core.ai.ConversationTurn;
import topicpromptui.core.config.ConfigModel;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static topicpromptui.core.ai.ConversationTurn.Speaker.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaudeApiImplTest {
    private final ClaudeApiImpl api = new ClaudeApiImpl("claude-opus", null, false);

    private static ConfigModel configWith(String context7Key) {
        return new ConfigModel() {
            @Override
            public String getProperty(String name) {
                return "context7.api.key".equals(name) ? context7Key : null;
            }

            @Override
            public Path getAppDataPath() {
                return null;
            }
        };
    }

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
    void assembleCapturesMcpToolCallAndKeepsText() {
        // With the Context7 MCP connector, the model streams an mcp_tool_use block (name/server on
        // content_block_start, input via input_json_delta) plus an mcp_tool_result block alongside the
        // answer text: only text_delta lands in the answer, only mcp_tool_use becomes a tool call.
        var deltas = new ArrayList<String>();
        var response = api.assemble(sse(
                "message_start", """
                        {"type": "message_start", "message": {"id": "msg_2", "usage": {"input_tokens": 50}}}""",
                "content_block_start", """
                        {"type": "content_block_start", "index": 0, "content_block": \
                        {"type": "mcp_tool_use", "name": "get-library-docs", "server_name": "context7"}}""",
                "content_block_delta", """
                        {"type": "content_block_delta", "index": 0, \
                        "delta": {"type": "input_json_delta", "partial_json": "{\\"library\\":"}}""",
                "content_block_delta", """
                        {"type": "content_block_delta", "index": 0, \
                        "delta": {"type": "input_json_delta", "partial_json": "\\"/facebook/react\\"}"}}""",
                "content_block_stop", """
                        {"type": "content_block_stop", "index": 0}""",
                "content_block_start", """
                        {"type": "content_block_start", "index": 1, \
                        "content_block": {"type": "mcp_tool_result", "tool_use_id": "mcptool_1"}}""",
                "content_block_stop", """
                        {"type": "content_block_stop", "index": 1}""",
                "content_block_delta", """
                        {"type": "content_block_delta", "index": 2, \
                        "delta": {"type": "text_delta", "text": "Per the docs, "}}""",
                "content_block_delta", """
                        {"type": "content_block_delta", "index": 2, \
                        "delta": {"type": "text_delta", "text": "use X."}}""",
                "message_delta", """
                        {"type": "message_delta", "delta": {"stop_reason": "end_turn"}, "usage": {"output_tokens": 30}}"""
        ), deltas::add);
        assertThat(deltas).containsExactly("Per the docs, ", "use X.");
        assertThat(response.text()).isEqualTo("Per the docs, use X.");
        assertThat(response.finishReason()).isEqualTo("end_turn");
        assertThat(response.toolCalls())
                .containsExactly("context7 · get-library-docs {\"library\":\"/facebook/react\"}");
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

    @Test
    void buildRequestBodyAttachesContext7WhenKeyPresent() {
        var body = api.buildRequestBody(List.of(new ConversationTurn(USER, "hi")), "ctx7-key");
        assertThat(body.mcp_servers()).singleElement().satisfies(server -> {
            assertThat(server.type()).isEqualTo("url");
            assertThat(server.name()).isEqualTo("context7");
            assertThat(server.url()).isEqualTo("https://mcp.context7.com/mcp");
            assertThat(server.authorization_token()).isEqualTo("ctx7-key");
        });
        assertThat(body.tools()).singleElement().satisfies(tool -> {
            assertThat(tool.type()).isEqualTo("mcp_toolset");
            assertThat(tool.mcp_server_name()).isEqualTo("context7");
        });
        assertThat(body.messages()).singleElement()
                .satisfies(m -> assertThat(m.role()).isEqualTo("user"));
    }

    @Test
    void buildRequestBodyOmitsContext7WhenKeyNull() {
        var body = api.buildRequestBody(List.of(new ConversationTurn(USER, "hi")), null);
        assertThat(body.mcp_servers()).isNull();
        assertThat(body.tools()).isNull();
    }

    @Test
    void context7KeyNullWhenDisabled() {
        // context7Enabled=false on this api instance; the config is never consulted.
        assertThat(api.context7Key()).isNull();
    }

    @Test
    void context7KeyReturnsConfiguredKeyWhenEnabled() {
        var enabled = new ClaudeApiImpl("claude-opus", null, true);
        enabled.configModel = configWith("the-key");
        assertThat(enabled.context7Key()).isEqualTo("the-key");
    }

    @Test
    void context7KeyNullWhenEnabledButBlankOrMissing() {
        var enabled = new ClaudeApiImpl("claude-opus", null, true);
        enabled.configModel = configWith("   ");
        assertThat(enabled.context7Key()).isNull();
        enabled.configModel = configWith(null);
        assertThat(enabled.context7Key()).isNull();
    }

    @Test
    void buildHttpRequestAddsMcpBetaHeaderWhenEnabled() {
        var request = api.buildHttpRequest("api-key", "{}", true);
        assertThat(request.headers().firstValue("x-api-key")).contains("api-key");
        assertThat(request.headers().firstValue("anthropic-version")).contains("2023-06-01");
        assertThat(request.headers().firstValue("anthropic-beta")).contains("mcp-client-2025-11-20");
    }

    @Test
    void buildHttpRequestOmitsMcpBetaHeaderWhenDisabled() {
        var request = api.buildHttpRequest("api-key", "{}", false);
        assertThat(request.headers().firstValue("anthropic-beta")).isEmpty();
    }

    @Test
    void assembleToleratesDeltaWithoutPayloadAndStopWithoutIndex() {
        var response = api.assemble(sse(
                "content_block_delta", """
                        {"type": "content_block_delta", "index": 0}""",
                "content_block_stop", """
                        {"type": "content_block_stop"}""",
                "content_block_delta", """
                        {"type": "content_block_delta", "index": 0, \
                        "delta": {"type": "text_delta", "text": "Hi"}}""",
                "message_delta", """
                        {"type": "message_delta", "delta": {"stop_reason": "end_turn"}, "usage": {"output_tokens": 1}}"""
        ), delta -> {
        });
        assertThat(response.text()).isEqualTo("Hi");
        assertThat(response.toolCalls()).isEmpty();
    }
}

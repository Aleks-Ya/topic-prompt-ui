package topicpromptui.core.ai.openai;

import com.google.gson.Gson;
import topicpromptui.core.ai.AiApiException;
import topicpromptui.core.ai.ConversationTurn;
import topicpromptui.core.config.ConfigModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static topicpromptui.core.ai.ConversationTurn.Speaker.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenAiApiImplTest {
    private final Gson gson = new Gson();
    private final OpenAiApiImpl api = new OpenAiApiImpl("gpt-5", null, false);

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
                        "output": [{"type": "message", "content": [{"text": "Full answer"}], "status": "completed"}], \
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
        var lines = sse(
                "response.failed", """
                        {"type": "response.failed", "response": {"id": "resp_4", "output": []}}"""
        );
        assertThatThrownBy(() -> api.assemble(lines, delta -> {
        }))
                .isInstanceOf(AiApiException.class)
                .hasMessageContaining("response.failed");
    }

    @Test
    void assembleThrowsWhenStreamEndsWithoutCompletedEvent() {
        var lines = sse(
                "response.output_text.delta", """
                        {"type": "response.output_text.delta", "delta": "Full "}"""
        );
        assertThatThrownBy(() -> api.assemble(lines, delta -> {
        }))
                .isInstanceOf(AiApiException.class)
                .hasMessageContaining("without a response.completed");
    }

    private static Stream<Arguments> parseResponseErrorCases() {
        return Stream.of(
                Arguments.of("truncated by token limit (status incomplete)", """
                        {"id": "resp_1", "output": [{"type": "message", "content": [{"text": "partial answ"}], \
                        "status": "incomplete"}], "usage": {"input_tokens": 10, "output_tokens": 5, "total_tokens": 15}}""",
                        "Message output not completed"),
                Arguments.of("multiple message outputs", """
                        {"id": "resp_2", "output": [{"type": "message", "content": [{"text": "answer 1"}], \
                        "status": "completed"}, {"type": "message", "content": [{"text": "answer 2"}], \
                        "status": "completed"}], "usage": {"input_tokens": 10, "output_tokens": 5, "total_tokens": 15}}""",
                        "Multiple message outputs"),
                Arguments.of("no message output (only MCP bookkeeping)", """
                        {"id": "resp_6", "output": [{"type": "mcp_list_tools", "status": "completed"}, \
                        {"type": "mcp_call", "status": "completed"}], \
                        "usage": {"input_tokens": 10, "output_tokens": 5, "total_tokens": 15}}""",
                        "No message output"),
                Arguments.of("message with multiple contents", """
                        {"id": "resp_7", "output": [{"type": "message", "status": "completed", \
                        "content": [{"text": "part 1"}, {"text": "part 2"}]}], \
                        "usage": {"input_tokens": 10, "output_tokens": 5, "total_tokens": 15}}""",
                        "Multiple contents"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("parseResponseErrorCases")
    void parseResponseThrows(String caseName, String json, String expectedMessage) {
        var responseBody = gson.fromJson(json, ResponseBody.class);
        assertThatThrownBy(() -> api.parseResponse(responseBody))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(expectedMessage);
    }

    @Test
    void parseResponseReturnsTextWhenCompleted() {
        var json = """
                {
                  "id": "resp_3",
                  "output": [
                    {"type": "message", "content": [{"text": "Full answer"}], "status": "completed"}
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

    @Test
    void parseResponseSelectsMessageAmongMcpOutputs() {
        // With the Context7 MCP tool enabled, the output array also carries mcp_list_tools / mcp_call /
        // reasoning items; parseResponse must pick the single "message" output, not choke on the extras.
        var json = """
                {
                  "id": "resp_5",
                  "output": [
                    {"type": "mcp_list_tools", "status": "completed"},
                    {"type": "reasoning", "status": "completed"},
                    {"type": "mcp_call", "status": "completed", "server_label": "context7", \
                     "name": "get-library-docs", "arguments": "{\\"library\\":\\"/facebook/react\\"}"},
                    {"type": "message", "content": [{"text": "The React docs say X."}], "status": "completed"}
                  ],
                  "usage": {"input_tokens": 100, "output_tokens": 40, "total_tokens": 140}
                }
                """;
        var responseBody = gson.fromJson(json, ResponseBody.class);
        var response = api.parseResponse(responseBody);
        assertThat(response.text()).isEqualTo("The React docs say X.");
        assertThat(response.finishReason()).isEqualTo("completed");
        assertThat(response.totalTokens()).isEqualTo(140);
        assertThat(response.toolCalls())
                .containsExactly("context7 · get-library-docs {\"library\":\"/facebook/react\"}");
    }

    @Test
    void parseResponseCollectsWebSearchCalls() {
        // web_search_call outputs carry no name/arguments; without the "action" mapping they would
        // render as "(unknown)".
        var json = """
                {
                  "id": "resp_8",
                  "output": [
                    {"type": "reasoning", "status": "completed"},
                    {"type": "web_search_call", "status": "completed", \
                     "action": {"type": "search", "query": "java 25 lts release"}},
                    {"type": "web_search_call", "status": "completed", \
                     "action": {"type": "open_page", "url": "https://openjdk.org/projects/jdk/25/"}},
                    {"type": "message", "content": [{"text": "Java 25 is the latest LTS."}], "status": "completed"}
                  ],
                  "usage": {"input_tokens": 100, "output_tokens": 40, "total_tokens": 140}
                }
                """;
        var responseBody = gson.fromJson(json, ResponseBody.class);
        var response = api.parseResponse(responseBody);
        assertThat(response.text()).isEqualTo("Java 25 is the latest LTS.");
        assertThat(response.toolCalls()).containsExactly(
                "openai · web_search java 25 lts release",
                "openai · web_search https://openjdk.org/projects/jdk/25/");
    }

    @Test
    void buildRequestBodyAttachesWebSearchWhenEnabled() {
        var enabled = new OpenAiApiImpl("gpt-5", null, true);
        // No Context7 key: web search still attaches on its own.
        var body = enabled.buildRequestBody("sys", List.of(new ConversationTurn(USER, "hi")), null);
        assertThat(body.tools()).singleElement().satisfies(tool -> {
            assertThat(tool.type()).isEqualTo("web_search");
            assertThat(tool.server_label()).isNull();
            assertThat(tool.require_approval()).isNull();
        });
        assertThat(gson.toJson(body)).contains("\"tools\":[{\"type\":\"web_search\"}]");
    }

    @Test
    void buildRequestBodyCombinesWebSearchAndContext7() {
        var enabled = new OpenAiApiImpl("gpt-5", null, true);
        var body = enabled.buildRequestBody("sys", List.of(new ConversationTurn(USER, "hi")), "ctx7-key");
        assertThat(body.tools()).extracting(Tool::type).containsExactly("web_search", "mcp");
    }

    @Test
    void buildRequestBodyAttachesContext7WhenKeyPresent() {
        var body = api.buildRequestBody("sys", List.of(new ConversationTurn(USER, "hi")), "ctx7-key");
        assertThat(body.instructions()).isEqualTo("sys");
        assertThat(body.tools()).singleElement().satisfies(tool -> {
            assertThat(tool.type()).isEqualTo("mcp");
            assertThat(tool.server_label()).isEqualTo("context7");
            assertThat(tool.server_url()).isEqualTo("https://mcp.context7.com/mcp");
            assertThat(tool.headers()).containsEntry("Authorization", "Bearer ctx7-key");
            assertThat(tool.require_approval()).isEqualTo("never");
        });
        assertThat(body.input()).singleElement()
                .satisfies(item -> assertThat(item.role()).isEqualTo("user"));
    }

    @Test
    void buildRequestBodyOmitsContext7WhenKeyNull() {
        var body = api.buildRequestBody(null, List.of(new ConversationTurn(USER, "hi")), null);
        assertThat(body.instructions()).isNull();
        assertThat(body.tools()).isNull();
    }

    @Test
    void context7KeyNullWhenDisabled() {
        // toolsEnabled=false on this api instance; the config is never consulted.
        assertThat(api.context7Key()).isNull();
    }

    @Test
    void context7KeyReturnsConfiguredKeyWhenEnabled() {
        var enabled = new OpenAiApiImpl("gpt-5", null, true);
        enabled.configModel = configWith("the-key");
        assertThat(enabled.context7Key()).isEqualTo("the-key");
    }

    @Test
    void context7KeyNullWhenEnabledButBlankOrMissing() {
        var enabled = new OpenAiApiImpl("gpt-5", null, true);
        enabled.configModel = configWith("   ");
        assertThat(enabled.context7Key()).isNull();
        enabled.configModel = configWith(null);
        assertThat(enabled.context7Key()).isNull();
    }

    @Test
    void buildHttpRequestSetsAuthAndContentTypeHeaders() {
        var request = api.buildHttpRequest("api-token", "{}");
        assertThat(request.headers().firstValue("Authorization")).contains("Bearer api-token");
        assertThat(request.headers().firstValue("Content-Type")).contains("application/json");
    }

}

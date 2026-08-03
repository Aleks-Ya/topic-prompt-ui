package topicpromptui.core.ai.claude;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import topicpromptui.core.ai.AiApi;
import topicpromptui.core.ai.ConversationTurn;
import topicpromptui.core.config.ConfigurationModule;
import topicpromptui.ui.model.storage.StorageModule;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static topicpromptui.core.ai.AiModule.CLAUDE_AI;
import static topicpromptui.core.ai.ConversationTurn.Speaker.MODEL;
import static topicpromptui.core.ai.ConversationTurn.Speaker.USER;

class ClaudeApiIT {
    private static final Logger log = LoggerFactory.getLogger(ClaudeApiIT.class);
    private final Injector injector = Guice.createInjector(new ClaudeModule(), new ConfigurationModule(), new StorageModule());
    private final AiApi api = injector.getInstance(Key.get(AiApi.class, Names.named(CLAUDE_AI)));

    @Test
    void send() {
        var response = api.send("What is the last Java version?");
        log.info("Response text: {}", response.text());
        log.info("Response ID: {}", response.responseId());
        log.info("Model ID: {}", response.modelId());
        log.info("Effort Level: {}", response.effortLevel());
        log.info("Finish Reason: {}", response.finishReason());
        log.info("Tokens: input={}, output={}, total={}", response.inputTokens(), response.outputTokens(), response.totalTokens());
        assertThat(response.text()).isNotBlank();
        assertThat(response.responseId()).isNotBlank();
        assertThat(response.modelId()).isNotBlank();
        assertThat(response.effortLevel()).isNotBlank();
        assertThat(response.finishReason()).isEqualTo("end_turn");
        assertThat(response.inputTokens()).isPositive();
        assertThat(response.outputTokens()).isPositive();
        assertThat(response.totalTokens()).isPositive();
    }

    @Test
    void sendMultiTurn() {
        var turns = List.of(
                new ConversationTurn(USER, "My favorite fruit is mango. Just acknowledge, don't say anything else."),
                new ConversationTurn(MODEL, "Got it."),
                new ConversationTurn(USER, "What fruit did I say was my favorite? Answer with just the fruit name."));
        var response = api.send(turns);
        log.info("Response text: {}", response.text());
        assertThat(response.text().toLowerCase()).contains("mango");
    }

    @Test
    void sendStreaming() {
        var deltas = new java.util.concurrent.CopyOnWriteArrayList<String>();
        var response = api.send("List the last 5 Java LTS versions with one sentence about each.", deltas::add);
        log.info("Deltas count: {}", deltas.size());
        assertThat(deltas).hasSizeGreaterThan(1);
        assertThat(String.join("", deltas)).isEqualTo(response.text());
        assertThat(response.responseId()).isNotBlank();
        assertThat(response.finishReason()).isEqualTo("end_turn");
        assertThat(response.totalTokens()).isPositive();
    }

    @Test
    void sendWithContext7Docs() {
        // Exercises the server-side Context7 MCP connector end to end (context7.api.key must be set):
        // a green run proves the MCP tool-use/tool-result blocks in the stream don't break assemble().
        var response = api.send("Using the Context7 documentation, briefly explain what the Context7 MCP "
                + "server provides for developers. Consult the library docs before answering.");
        log.info("Response text: {}", response.text());
        log.info("Finish Reason: {}", response.finishReason());
        log.info("Tool Calls: {}", response.toolCalls());
        assertThat(response.text()).isNotBlank();
        assertThat(response.finishReason()).isIn("end_turn", "pause_turn");
        assertThat(response.toolCalls()).isNotEmpty();
    }

    @Test
    void error() {
        assertThatThrownBy(() -> api.send((String) null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("invalid_request_error");
    }
}

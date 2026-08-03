package topicpromptui.core.ai.openai;

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
import topicpromptui.core.domain.AnswerType;
import topicpromptui.core.prompt.PromptFactory;
import topicpromptui.core.prompt.PromptModule;
import topicpromptui.ui.model.storage.StorageModule;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static topicpromptui.core.ai.AiModule.OPEN_AI;
import static topicpromptui.core.ai.AiModule.OPEN_AI_GRAMMAR;
import static topicpromptui.core.ai.ConversationTurn.Speaker.MODEL;
import static topicpromptui.core.ai.ConversationTurn.Speaker.USER;
import static topicpromptui.core.domain.InteractionType.DEFINITION;

class OpenAiApiIT {
    private static final Logger log = LoggerFactory.getLogger(OpenAiApiIT.class);
    private final Injector injector = Guice.createInjector(new OpenAiModule(), new ConfigurationModule(),
            new StorageModule(), new PromptModule());
    private final AiApi api = injector.getInstance(Key.get(AiApi.class, Names.named(OPEN_AI)));
    private final AiApi grammarApi = injector.getInstance(Key.get(AiApi.class, Names.named(OPEN_AI_GRAMMAR)));
    private final PromptFactory promptFactory = injector.getInstance(PromptFactory.class);

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
        assertThat(response.finishReason()).isEqualTo("completed");
        assertThat(response.inputTokens()).isPositive();
        assertThat(response.outputTokens()).isPositive();
        assertThat(response.totalTokens()).isPositive();
    }

    @Test
    void sendGrammar() {
        var response = grammarApi.send("What is the last Java version?");
        log.info("Response text: {}", response.text());
        log.info("responseId: {}", response.responseId());
        log.info("Model ID: {}", response.modelId());
        log.info("Effort Level: {}", response.effortLevel());
        log.info("Finish Reason: {}", response.finishReason());
        log.info("tokens: input={}, output={}, total={}", response.inputTokens(), response.outputTokens(), response.totalTokens());
        assertThat(response.text()).isNotBlank();
        assertThat(response.responseId()).isNotBlank();
        assertThat(response.modelId()).isNotBlank();
        assertThat(response.effortLevel()).isNotBlank();
        assertThat(response.finishReason()).isEqualTo("completed");
        assertThat(response.inputTokens()).isPositive();
        assertThat(response.outputTokens()).isPositive();
        assertThat(response.totalTokens()).isPositive();
    }

    @Test
    void definitionOpenAi() {
        var system = promptFactory.getSystemPrompt(DEFINITION, "AWS S3", AnswerType.OPEN_AI).orElse(null);
        var prompt = promptFactory.getPrompt(DEFINITION, "Bucket", AnswerType.OPEN_AI).orElseThrow();
        var response = api.send(system, List.of(new ConversationTurn(USER, prompt)), _ -> {
        });
        log.info("Response text: {}", response.text());
        log.info("Response ID: {}", response.responseId());
        assertThat(response.text()).isNotBlank();
        assertThat(response.responseId()).isNotBlank();
    }

    @Test
    void definitionGrammar() {
        var system = promptFactory.getSystemPrompt(DEFINITION, "AWS S3", AnswerType.GRAMMAR).orElse(null);
        var prompt = promptFactory.getPrompt(DEFINITION, "Bucket", AnswerType.GRAMMAR).orElseThrow();
        var response = grammarApi.send(system, List.of(new ConversationTurn(USER, prompt)), _ -> {
        });
        log.info("Response text: {}", response.text());
        log.info("responseId: {}", response.responseId());
        assertThat(response.text()).isNotBlank();
        assertThat(response.responseId()).isNotBlank();
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
        assertThat(response.finishReason()).isEqualTo("completed");
        assertThat(response.totalTokens()).isPositive();
    }

    @Test
    void sendWithContext7Docs() {
        // Exercises the server-side Context7 MCP tool end to end (context7.api.key must be set): a green
        // run proves the extra mcp_list_tools/mcp_call outputs don't break parseResponse's selection.
        var response = api.send("Using the Context7 documentation, briefly explain what the Context7 MCP "
                + "server provides for developers. Consult the library docs before answering.");
        log.info("Response text: {}", response.text());
        log.info("Finish Reason: {}", response.finishReason());
        log.info("Tool Calls: {}", response.toolCalls());
        assertThat(response.text()).isNotBlank();
        assertThat(response.finishReason()).isEqualTo("completed");
        assertThat(response.toolCalls()).isNotEmpty();
    }

    @Test
    void error() {
        assertThatThrownBy(() -> api.send((String) null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("invalid_request_error");
    }

    @Test
    void errorGrammar() {
        assertThatThrownBy(() -> grammarApi.send((String) null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("invalid_request_error");
    }
}
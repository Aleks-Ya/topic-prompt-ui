package gptui.core.ai.openai;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import gptui.core.ai.AiApi;
import gptui.core.ai.ConversationTurn;
import gptui.core.storagefilesystem.AnswerType;
import gptui.ui.model.config.ConfigurationModule;
import gptui.ui.model.question.prompt.PromptFactory;
import gptui.ui.model.question.prompt.PromptModule;
import gptui.ui.model.storage.StorageModule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static gptui.core.ai.AiModule.OPEN_AI;
import static gptui.core.ai.AiModule.OPEN_AI_GRAMMAR;
import static gptui.core.ai.ConversationTurn.Speaker.MODEL;
import static gptui.core.ai.ConversationTurn.Speaker.USER;
import static gptui.core.storagefilesystem.InteractionType.DEFINITION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenAiApiIT {
    private final Injector injector = Guice.createInjector(new OpenAiModule(), new ConfigurationModule(),
            new StorageModule(), new PromptModule());
    private final AiApi api = injector.getInstance(Key.get(AiApi.class, Names.named(OPEN_AI)));
    private final AiApi grammarApi = injector.getInstance(Key.get(AiApi.class, Names.named(OPEN_AI_GRAMMAR)));
    private final PromptFactory promptFactory = injector.getInstance(PromptFactory.class);

    @Test
    void send() {
        var response = api.send("What is the last Java version?");
        System.out.println(response.text());
        System.out.println("responseId: " + response.responseId());
        System.out.println("modelId: " + response.modelId());
        System.out.println("modelParams: " + response.modelParams());
        System.out.println("tokens: input=" + response.inputTokens() + " output=" + response.outputTokens()
                + " total=" + response.totalTokens());
        assertThat(response.text()).isNotBlank();
        assertThat(response.responseId()).isNotBlank();
        assertThat(response.modelId()).isNotBlank();
        assertThat(response.modelParams()).isNotBlank();
        assertThat(response.inputTokens()).isPositive();
        assertThat(response.outputTokens()).isPositive();
        assertThat(response.totalTokens()).isPositive();
    }

    @Test
    void sendGrammar() {
        var response = grammarApi.send("What is the last Java version?");
        System.out.println(response.text());
        System.out.println("responseId: " + response.responseId());
        System.out.println("modelId: " + response.modelId());
        System.out.println("modelParams: " + response.modelParams());
        System.out.println("tokens: input=" + response.inputTokens() + " output=" + response.outputTokens()
                + " total=" + response.totalTokens());
        assertThat(response.text()).isNotBlank();
        assertThat(response.responseId()).isNotBlank();
        assertThat(response.modelId()).isNotBlank();
        assertThat(response.modelParams()).isNotBlank();
        assertThat(response.inputTokens()).isPositive();
        assertThat(response.outputTokens()).isPositive();
        assertThat(response.totalTokens()).isPositive();
    }

    @Test
    void definitionOpenAi() {
        var prompt = promptFactory.getPrompt(DEFINITION, "AWS S3", "Bucket", AnswerType.OPEN_AI).orElseThrow();
        var response = api.send(prompt);
        System.out.println(response.text());
        System.out.println("responseId: " + response.responseId());
        assertThat(response.text()).isNotBlank();
        assertThat(response.responseId()).isNotBlank();
    }

    @Test
    void definitionGrammar() {
        var prompt = promptFactory.getPrompt(DEFINITION, "AWS S3", "Bucket", AnswerType.GRAMMAR).orElseThrow();
        var response = grammarApi.send(prompt);
        System.out.println(response.text());
        System.out.println("responseId: " + response.responseId());
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
        System.out.println(response.text());
        assertThat(response.text().toLowerCase()).contains("mango");
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
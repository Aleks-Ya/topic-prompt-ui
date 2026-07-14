package gptui.core.ai.claude;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import gptui.core.ai.AiApi;
import gptui.core.ai.ConversationTurn;
import gptui.core.config.ConfigurationModule;
import gptui.ui.model.storage.StorageModule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static gptui.core.ai.AiModule.CLAUDE_AI;
import static gptui.core.ai.ConversationTurn.Speaker.MODEL;
import static gptui.core.ai.ConversationTurn.Speaker.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaudeApiIT {
    private final Injector injector = Guice.createInjector(new ClaudeModule(), new ConfigurationModule(), new StorageModule());
    private final AiApi api = injector.getInstance(Key.get(AiApi.class, Names.named(CLAUDE_AI)));

    @Test
    void send() {
        var response = api.send("What is the last Java version?");
        System.out.println(response.text());
        System.out.println("responseId: " + response.responseId());
        System.out.println("modelId: " + response.modelId());
        System.out.println("effortLevel: " + response.effortLevel());
        System.out.println("finishReason: " + response.finishReason());
        System.out.println("tokens: input=" + response.inputTokens() + " output=" + response.outputTokens()
                + " total=" + response.totalTokens());
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
        System.out.println(response.text());
        assertThat(response.text().toLowerCase()).contains("mango");
    }

    @Test
    void sendStreaming() {
        var deltas = new java.util.concurrent.CopyOnWriteArrayList<String>();
        var response = api.send("List the last 5 Java LTS versions with one sentence about each.", deltas::add);
        System.out.println("deltas: " + deltas.size());
        assertThat(deltas.size()).isGreaterThan(1);
        assertThat(String.join("", deltas)).isEqualTo(response.text());
        assertThat(response.responseId()).isNotBlank();
        assertThat(response.finishReason()).isEqualTo("end_turn");
        assertThat(response.totalTokens()).isPositive();
    }

    @Test
    void error() {
        assertThatThrownBy(() -> api.send((String) null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("invalid_request_error");
    }
}

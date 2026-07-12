package gptui.core.ai.claude;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import gptui.core.ai.AiApi;
import gptui.core.ai.ConversationTurn;
import gptui.ui.model.config.ConfigurationModule;
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
}

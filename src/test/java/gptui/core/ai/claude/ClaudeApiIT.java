package gptui.core.ai.claude;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import gptui.core.ai.AiApi;
import gptui.ui.model.config.ConfigurationModule;
import gptui.ui.model.storage.StorageModule;
import org.junit.jupiter.api.Test;

import static gptui.core.ai.AiModule.CLAUDE_AI;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaudeApiIT {
    private final Injector injector = Guice.createInjector(new ClaudeModule(), new ConfigurationModule(), new StorageModule());
    private final AiApi api = injector.getInstance(Key.get(AiApi.class, Names.named(CLAUDE_AI)));

    @Test
    void send() {
        var response = api.send("What is the last Java version?");
        System.out.println(response.text());
        System.out.println("responseId: " + response.responseId());
    }

    @Test
    void error() {
        assertThatThrownBy(() -> api.send(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("invalid_request_error");
    }
}

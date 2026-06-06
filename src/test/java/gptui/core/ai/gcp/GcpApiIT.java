package gptui.core.ai.gcp;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import gptui.core.ai.AiApi;
import gptui.model.config.ConfigurationModule;
import gptui.model.question.prompt.PromptFactory;
import gptui.model.question.prompt.PromptModule;
import gptui.model.storage.StorageModule;
import org.junit.jupiter.api.Test;

import static gptui.core.ai.AiModule.GCP_AI;
import static gptui.model.storage.AnswerType.GCP;
import static gptui.model.storage.InteractionType.DEFINITION;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GcpApiIT {
    private final Injector injector = Guice.createInjector(new GcpModule(), new ConfigurationModule(),
            new StorageModule(), new PromptModule());
    private final AiApi api = injector.getInstance(Key.get(AiApi.class, Names.named(GCP_AI)));
    private final PromptFactory promptFactory = injector.getInstance(PromptFactory.class);

    @Test
    void send() {
        var response = api.send("What is the last Java version?", 100);
        System.out.println(response);
    }

    @Test
    void definition() {
        var prompt = promptFactory.getPrompt(DEFINITION, "AWS S3", "Bucket", GCP).orElseThrow();
        var response = api.send(prompt, 100);
        System.out.println(response);
    }

    @Test
    void error() {
        assertThatThrownBy(() -> api.send(null, 100))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("INVALID_ARGUMENT");
    }
}
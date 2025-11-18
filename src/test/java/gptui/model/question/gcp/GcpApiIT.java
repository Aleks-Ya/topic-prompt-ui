package gptui.model.question.gcp;

import com.google.inject.Guice;
import com.google.inject.Injector;
import gptui.model.config.ConfigurationModule;
import gptui.model.question.prompt.PromptFactory;
import gptui.model.question.prompt.PromptModule;
import gptui.model.storage.StorageModule;
import org.junit.jupiter.api.Test;

import static gptui.model.storage.AnswerType.GCP;
import static gptui.model.storage.InteractionType.DEFINITION;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GcpApiIT {
    private final Injector injector = Guice.createInjector(new GcpModule(), new ConfigurationModule(),
            new StorageModule(), new PromptModule());
    private final GcpApi api = injector.getInstance(GcpApi.class);
    private final PromptFactory promptFactory = injector.getInstance(PromptFactory.class);

    @Test
    void send() {
        var response = api.send("What is the last Java version?", 50);
        System.out.println(response);
    }

    @Test
    void definition() {
        var prompt = promptFactory.getPrompt(DEFINITION, "AWS S3", "Bucket", GCP).orElseThrow();
        var response = api.send(prompt, 50);
        System.out.println(response);
    }

    @Test
    void error() {
        assertThatThrownBy(() -> api.send(null, 50))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("INVALID_ARGUMENT");
    }
}
package gptui.core.ai.openai;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import gptui.core.ai.AiApi;
import gptui.core.storagefilesystem.AnswerType;
import gptui.ui.model.config.ConfigurationModule;
import gptui.ui.model.question.prompt.PromptFactory;
import gptui.ui.model.question.prompt.PromptModule;
import gptui.ui.model.storage.StorageModule;
import org.junit.jupiter.api.Test;

import static gptui.core.ai.AiModule.OPEN_AI;
import static gptui.core.storagefilesystem.InteractionType.DEFINITION;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenAiApiIT {
    private final Injector injector = Guice.createInjector(new OpenAiModule(), new ConfigurationModule(),
            new StorageModule(), new PromptModule());
    private final AiApi api = injector.getInstance(Key.get(AiApi.class, Names.named(OPEN_AI)));
    private final PromptFactory promptFactory = injector.getInstance(PromptFactory.class);

    @Test
    void send() {
        var response = api.send("What is the last Java version?", 50);
        System.out.println(response);
    }

    @Test
    void definitionOpenAi() {
        var prompt = promptFactory.getPrompt(DEFINITION, "AWS S3", "Bucket", AnswerType.OPEN_AI).orElseThrow();
        var response = api.send(prompt, 50);
        System.out.println(response);
    }

    @Test
    void error() {
        assertThatThrownBy(() -> api.send(null, 50))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("invalid_request_error");
    }
}
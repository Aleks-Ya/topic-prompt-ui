package gptui.model.question.openai.responses;

import com.google.inject.Guice;
import com.google.inject.Injector;
import gptui.model.config.ConfigurationModule;
import gptui.model.question.openai.OpenAiApi;
import gptui.model.question.prompt.PromptFactory;
import gptui.model.question.prompt.PromptModule;
import gptui.model.storage.StorageModule;
import org.junit.jupiter.api.Test;

import static gptui.model.storage.AnswerType.SHORT;
import static gptui.model.storage.InteractionType.DEFINITION;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResponsesOpenAiApiIT {
    private final Injector injector = Guice.createInjector(new ResponsesOpenAiModule(), new ConfigurationModule(),
            new StorageModule(), new PromptModule());
    private final OpenAiApi api = injector.getInstance(OpenAiApi.class);
    private final PromptFactory promptFactory = injector.getInstance(PromptFactory.class);

    @Test
    void send() {
        var response = api.send("What is the last Java version?", 50);
        System.out.println(response);
    }

    @Test
    void definitionShort() {
        var prompt = promptFactory.getPrompt(DEFINITION, "AWS S3", "Bucket", SHORT).orElseThrow();
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
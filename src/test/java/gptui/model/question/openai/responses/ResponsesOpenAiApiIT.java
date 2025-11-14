package gptui.model.question.openai.responses;

import com.google.inject.Guice;
import gptui.model.config.ConfigurationModule;
import gptui.model.question.openai.OpenAiApi;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResponsesOpenAiApiIT {
    private final OpenAiApi api = Guice.createInjector(new ConfigurationModule(), new ResponsesOpenAiModule())
            .getInstance(OpenAiApi.class);

    @Test
    void send() {
        var response = api.send("What is the last Java version?", 50);
        System.out.println(response);
    }

    @Test
    void error() {
        assertThatThrownBy(() -> api.send(null, 50))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("invalid_request_error");
    }
}
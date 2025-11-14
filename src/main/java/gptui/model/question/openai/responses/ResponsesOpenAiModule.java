package gptui.model.question.openai.responses;

import com.google.inject.AbstractModule;
import gptui.model.question.openai.OpenAiApi;

public class ResponsesOpenAiModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(OpenAiApi.class).to(OpenAiApiImpl.class);
    }
}

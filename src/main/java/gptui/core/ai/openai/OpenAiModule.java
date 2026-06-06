package gptui.core.ai.openai;

import com.google.inject.AbstractModule;

public class OpenAiModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(OpenAiApi.class).to(OpenAiApiImpl.class);
    }
}

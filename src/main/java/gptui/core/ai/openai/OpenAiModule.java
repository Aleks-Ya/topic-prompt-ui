package gptui.core.ai.openai;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import gptui.core.ai.AiApi;

import static gptui.core.ai.AiModule.OPEN_AI;

public class OpenAiModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AiApi.class).annotatedWith(Names.named(OPEN_AI)).to(OpenAiApiImpl.class);
    }
}

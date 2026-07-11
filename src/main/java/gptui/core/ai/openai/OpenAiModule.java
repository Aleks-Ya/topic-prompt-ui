package gptui.core.ai.openai;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import gptui.core.ai.AiApi;

import static gptui.core.ai.AiModule.OPEN_AI;
import static gptui.core.ai.AiModule.OPEN_AI_GRAMMAR;

public class OpenAiModule extends AbstractModule {
    private static final String MODEL = "gpt-5.6-sol";
    private static final String GRAMMAR_MODEL = "gpt-5.6-luna";

    @Override
    protected void configure() {
        bind(AiApi.class).annotatedWith(Names.named(OPEN_AI)).toInstance(new OpenAiApiImpl(MODEL));
        bind(AiApi.class).annotatedWith(Names.named(OPEN_AI_GRAMMAR)).toInstance(new OpenAiApiImpl(GRAMMAR_MODEL));
    }
}

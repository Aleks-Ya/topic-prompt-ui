package gptui.core.ai.openai;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import gptui.core.ai.AiApi;

import static gptui.core.ai.AiModule.OPEN_AI;
import static gptui.core.ai.AiModule.OPEN_AI_GRAMMAR;

public class OpenAiModule extends AbstractModule {
    private static final String MODEL = "gpt-5.6-sol";
    private static final String GRAMMAR_MODEL = "gpt-5.6-luna";
    private static final ReasoningEffort EFFORT = ReasoningEffort.XHIGH;
    private static final ReasoningEffort GRAMMAR_EFFORT = ReasoningEffort.MEDIUM;

    @Override
    protected void configure() {
        bind(AiApi.class).annotatedWith(Names.named(OPEN_AI)).toInstance(new OpenAiApiImpl(MODEL, EFFORT));
        bind(AiApi.class).annotatedWith(Names.named(OPEN_AI_GRAMMAR))
                .toInstance(new OpenAiApiImpl(GRAMMAR_MODEL, GRAMMAR_EFFORT));
    }
}

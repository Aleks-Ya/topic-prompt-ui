package topicpromptui.core.ai.xai;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import topicpromptui.core.ai.AiApi;

import static topicpromptui.core.ai.AiModule.XAI;

public class XaiModule extends AbstractModule {
    private static final String MODEL = "grok-4.3";
    private static final ReasoningEffort EFFORT = ReasoningEffort.MEDIUM;

    @Override
    protected void configure() {
        bind(AiApi.class).annotatedWith(Names.named(XAI)).toInstance(new XaiApiImpl(MODEL, EFFORT, true));
    }
}

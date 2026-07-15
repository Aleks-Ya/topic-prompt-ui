package topicpromptui.core.ai.claude;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import topicpromptui.core.ai.AiApi;

import static topicpromptui.core.ai.AiModule.CLAUDE_AI;

public class ClaudeModule extends AbstractModule {
    private static final String MODEL = "claude-opus-4-8";
    private static final Effort EFFORT = Effort.XHIGH;

    @Override
    protected void configure() {
        bind(AiApi.class).annotatedWith(Names.named(CLAUDE_AI)).toInstance(new ClaudeApiImpl(MODEL, EFFORT));
    }
}

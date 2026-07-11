package gptui.core.ai.claude;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import gptui.core.ai.AiApi;

import static gptui.core.ai.AiModule.CLAUDE_AI;

public class ClaudeModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AiApi.class).annotatedWith(Names.named(CLAUDE_AI)).to(ClaudeApiImpl.class);
    }
}

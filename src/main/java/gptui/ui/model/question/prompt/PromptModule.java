package gptui.ui.model.question.prompt;

import com.google.inject.AbstractModule;

public class PromptModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(PromptFactory.class).to(PromptFactoryImpl.class);
    }
}

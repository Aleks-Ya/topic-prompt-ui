package topicpromptui.ui.viewmodel.uiapp;

import com.google.inject.AbstractModule;

public class TopicPromptUiApplicationVmModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TopicPromptUiApplicationVmController.class).to(TopicPromptUiApplicationVmImpl.class);
    }
}

package topicpromptui.ui.viewmodel.ui;

import com.google.inject.AbstractModule;

public class TopicPromptUiVmModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TopicPromptUiVmController.class).to(TopicPromptUiVmImpl.class);
    }
}

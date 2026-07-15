package topicpromptui.ui.viewmodel.topic;

import com.google.inject.AbstractModule;

public class TopicVmModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TopicVmController.class).to(TopicVmImpl.class);
        bind(TopicVmMediator.class).to(TopicVmImpl.class);
    }
}

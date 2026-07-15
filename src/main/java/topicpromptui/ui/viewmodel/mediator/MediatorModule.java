package topicpromptui.ui.viewmodel.mediator;

import com.google.inject.AbstractModule;

public class MediatorModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(MediatorImpl.class);
        bind(HistoryMediator.class).to(MediatorImpl.class);
        bind(QuestionMediator.class).to(MediatorImpl.class);
        bind(TopicMediator.class).to(MediatorImpl.class);
        bind(AnswerMediator.class).to(MediatorImpl.class);
        bind(TopicPromptUiMediator.class).to(MediatorImpl.class);
        bind(TopicPromptUiApplicationMediator.class).to(MediatorImpl.class);
    }
}

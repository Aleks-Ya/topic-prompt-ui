package topicpromptui.ui.view;

import com.google.inject.AbstractModule;

public class ViewModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TopicPromptUiController.class);
        bind(HistoryController.class);
        bind(TopicController.class);
        bind(QuestionController.class);
        bind(AnswerController.class);
    }
}

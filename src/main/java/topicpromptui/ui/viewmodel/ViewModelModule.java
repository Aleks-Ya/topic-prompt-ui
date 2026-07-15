package topicpromptui.ui.viewmodel;

import com.google.inject.AbstractModule;
import topicpromptui.ui.viewmodel.answer.AnswerVmModule;
import topicpromptui.ui.viewmodel.history.HistoryVmModule;
import topicpromptui.ui.viewmodel.mediator.MediatorModule;
import topicpromptui.ui.viewmodel.question.QuestionVmModule;
import topicpromptui.ui.viewmodel.topic.TopicVmModule;
import topicpromptui.ui.viewmodel.ui.TopicPromptUiVmModule;
import topicpromptui.ui.viewmodel.uiapp.TopicPromptUiApplicationVmModule;

public class ViewModelModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new QuestionVmModule());
        install(new TopicVmModule());
        install(new HistoryVmModule());
        install(new AnswerVmModule());
        install(new TopicPromptUiVmModule());
        install(new TopicPromptUiApplicationVmModule());
        install(new MediatorModule());
    }
}

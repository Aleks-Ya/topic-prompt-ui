package gptui.ui.viewmodel;

import com.google.inject.AbstractModule;
import gptui.ui.viewmodel.answer.AnswerVmModule;
import gptui.ui.viewmodel.history.HistoryVmModule;
import gptui.ui.viewmodel.mediator.MediatorModule;
import gptui.ui.viewmodel.question.QuestionVmModule;
import gptui.ui.viewmodel.topic.TopicVmModule;
import gptui.ui.viewmodel.ui.GptUiVmModule;
import gptui.ui.viewmodel.uiapp.GptUiApplicationVmModule;

public class ViewModelModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new QuestionVmModule());
        install(new TopicVmModule());
        install(new HistoryVmModule());
        install(new AnswerVmModule());
        install(new GptUiVmModule());
        install(new GptUiApplicationVmModule());
        install(new MediatorModule());
    }
}

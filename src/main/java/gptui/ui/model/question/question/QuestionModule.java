package gptui.ui.model.question.question;

import com.google.inject.AbstractModule;
import gptui.ui.model.question.QuestionModel;
import gptui.ui.model.question.sound.SoundModule;

public class QuestionModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new SoundModule());
        bind(QuestionModel.class).to(QuestionModelImpl.class);
        bind(FormatConverter.class);
        bind(FollowUpHistoryBuilder.class);
    }
}

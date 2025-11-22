package gptui.model.question.question;

import com.google.inject.AbstractModule;
import gptui.model.question.QuestionModel;
import gptui.model.question.sound.SoundModule;

public class QuestionModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new SoundModule());
        bind(QuestionModel.class).to(QuestionModelImpl.class);
        bind(FormatConverter.class);
    }
}

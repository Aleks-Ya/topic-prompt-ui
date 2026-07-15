package topicpromptui.ui.model.question.question;

import com.google.inject.AbstractModule;
import topicpromptui.ui.model.question.QuestionModel;
import topicpromptui.ui.model.question.sound.SoundModule;

public class QuestionModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new SoundModule());
        bind(QuestionModel.class).to(QuestionModelImpl.class);
        bind(FormatConverter.class);
        bind(FollowUpHistoryBuilder.class);
    }
}

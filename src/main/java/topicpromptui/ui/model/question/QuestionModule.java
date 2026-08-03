package topicpromptui.ui.model.question;

import com.google.inject.AbstractModule;
import topicpromptui.core.sound.SoundModule;

public class QuestionModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new SoundModule());
        bind(QuestionModel.class).to(QuestionModelImpl.class);
        bind(FormatConverter.class);
        bind(FollowUpHistoryBuilder.class);
    }
}

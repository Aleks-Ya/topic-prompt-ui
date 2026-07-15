package topicpromptui.ui.model.question.sound;

import topicpromptui.BaseTest;
import topicpromptui.core.storagefilesystem.AnswerType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class SoundServiceTest extends BaseTest {
    private final SoundService soundService = injector.getInstance(SoundService.class);

    @Test
    void beenOnAnswer() {
        assertThatCode(() -> soundService.beenOnAnswer(AnswerType.GRAMMAR)).doesNotThrowAnyException();
    }
}
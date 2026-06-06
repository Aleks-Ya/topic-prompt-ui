package gptui.ui.model.question.sound;

import gptui.BaseTest;
import gptui.ui.model.storage.AnswerType;
import org.junit.jupiter.api.Test;

class SoundServiceTest extends BaseTest {
    private final SoundService soundService = injector.getInstance(SoundService.class);

    @Test
    void beenOnAnswer() {
        soundService.beenOnAnswer(AnswerType.GRAMMAR);
    }
}
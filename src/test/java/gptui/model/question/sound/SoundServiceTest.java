package gptui.model.question.sound;

import gptui.BaseTest;
import gptui.model.storage.AnswerType;
import org.junit.jupiter.api.Test;

class SoundServiceTest extends BaseTest {
    private final SoundService soundService = injector.getInstance(SoundService.class);

    @Test
    void beenOnAnswer() {
        soundService.beenOnAnswer(AnswerType.GRAMMAR);
    }
}
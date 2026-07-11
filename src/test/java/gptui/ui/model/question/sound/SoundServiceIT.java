package gptui.ui.model.question.sound;

import com.google.inject.Guice;
import com.google.inject.Injector;
import gptui.core.storagefilesystem.AnswerType;
import org.junit.jupiter.api.Test;

class SoundServiceIT {
    private final Injector injector = Guice.createInjector(new SoundModule());
    private final SoundService soundService = injector.getInstance(SoundService.class);

    @Test
    void beenOnAnswerGrammar() throws InterruptedException {
        soundService.beenOnAnswer(AnswerType.GRAMMAR);
        Thread.sleep(3000);
    }

    @Test
    void beenOnAnswerShort() throws InterruptedException {
        soundService.beenOnAnswer(AnswerType.SHORT);
        Thread.sleep(3000);
    }

    @Test
    void beenOnAnswerClaude() throws InterruptedException {
        soundService.beenOnAnswer(AnswerType.CLAUDE);
        Thread.sleep(3000);
    }

    @Test
    void beenOnAnswerGcp() throws InterruptedException {
        soundService.beenOnAnswer(AnswerType.GCP);
        Thread.sleep(3000);
    }
}
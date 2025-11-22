package gptui.model.question.sound;

import com.google.inject.Guice;
import com.google.inject.Injector;
import gptui.model.storage.AnswerType;
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
    void beenOnAnswerLong() throws InterruptedException {
        soundService.beenOnAnswer(AnswerType.LONG);
        Thread.sleep(3000);
    }

    @Test
    void beenOnAnswerGcp() throws InterruptedException {
        soundService.beenOnAnswer(AnswerType.GCP);
        Thread.sleep(3000);
    }
}
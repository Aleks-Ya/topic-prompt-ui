package gptui.ui.model.question.sound;

import com.google.inject.Guice;
import com.google.inject.Injector;
import gptui.core.storagefilesystem.AnswerType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class SoundServiceIT {
    private final Injector injector = Guice.createInjector(new SoundModule());
    private final SoundService soundService = injector.getInstance(SoundService.class);

    // Thread.sleep is intentional here, not a substitute for polling: these tests play an audio
    // clip for a human to listen to, and the sleep just keeps the JVM alive long enough for
    // playback to finish before the test (and process) exits.
    @Test
    void beenOnAnswerGrammar() throws InterruptedException {
        assertThatCode(() -> soundService.beenOnAnswer(AnswerType.GRAMMAR)).doesNotThrowAnyException();
        Thread.sleep(3000);
    }

    @Test
    void beenOnAnswerOpenAi() throws InterruptedException {
        assertThatCode(() -> soundService.beenOnAnswer(AnswerType.OPEN_AI)).doesNotThrowAnyException();
        Thread.sleep(3000);
    }

    @Test
    void beenOnAnswerClaude() throws InterruptedException {
        assertThatCode(() -> soundService.beenOnAnswer(AnswerType.CLAUDE)).doesNotThrowAnyException();
        Thread.sleep(3000);
    }

    @Test
    void beenOnAnswerGcp() throws InterruptedException {
        assertThatCode(() -> soundService.beenOnAnswer(AnswerType.GCP)).doesNotThrowAnyException();
        Thread.sleep(3000);
    }
}
package gptui.model.question.sound;

import gptui.model.storage.AnswerType;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SoundServiceMock implements SoundService {
    private static final Logger log = LoggerFactory.getLogger(SoundServiceMock.class);

    @Override
    public synchronized void beenOnAnswer(AnswerType answerType) {
        log.debug("Beep on answer: {}", answerType);
    }
}

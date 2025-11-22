package gptui.model.question.sound;

import gptui.model.storage.AnswerType;
import jakarta.inject.Singleton;
import javafx.scene.media.AudioClip;

import static gptui.util.ResourceUtils.resourcePath;

@Singleton
class SoundServiceImpl implements SoundService {
    private static final Double volume = 0.1;
    private final AudioClip beep1;
    private final AudioClip beep2;
    private final AudioClip beep3;

    public SoundServiceImpl() {
        beep1 = new AudioClip(resourcePath(getClass(), "beep-1.wav"));
        beep2 = new AudioClip(resourcePath(getClass(), "beep-2.wav"));
        beep3 = new AudioClip(resourcePath(getClass(), "beep-3.wav"));
    }

    @Override
    public synchronized void beenOnAnswer(AnswerType answerType) {
        switch (answerType) {
            case GRAMMAR -> beep1.play(volume);
            case SHORT -> beep2.play(volume);
            case LONG -> beep3.play(volume);
        }
    }
}

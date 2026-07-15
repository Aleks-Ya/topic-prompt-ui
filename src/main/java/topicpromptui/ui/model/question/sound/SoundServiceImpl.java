package topicpromptui.ui.model.question.sound;

import topicpromptui.core.storagefilesystem.AnswerType;
import jakarta.inject.Singleton;
import javafx.scene.media.AudioClip;

import static topicpromptui.core.util.ResourceUtils.resourcePath;

@Singleton
class SoundServiceImpl implements SoundService {
    private static final Double VOLUME = 0.1;
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
            case GRAMMAR -> beep1.play(VOLUME);
            case OPEN_AI -> beep2.play(VOLUME);
            case CLAUDE -> beep3.play(VOLUME);
            case GCP -> {
                // No dedicated beep sound for GCP answers.
            }
        }
    }
}

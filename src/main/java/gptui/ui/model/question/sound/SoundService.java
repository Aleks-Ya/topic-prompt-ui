package gptui.ui.model.question.sound;

import gptui.core.storagefilesystem.AnswerType;

public interface SoundService {
    void beenOnAnswer(AnswerType answerType);
}

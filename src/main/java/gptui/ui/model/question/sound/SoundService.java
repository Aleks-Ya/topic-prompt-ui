package gptui.ui.model.question.sound;

import gptui.ui.model.storage.AnswerType;

public interface SoundService {
    void beenOnAnswer(AnswerType answerType);
}

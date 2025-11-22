package gptui.model.question.sound;

import gptui.model.storage.AnswerType;

public interface SoundService {
    void beenOnAnswer(AnswerType answerType);
}

package topicpromptui.ui.model.question.sound;

import topicpromptui.core.domain.AnswerType;

public interface SoundService {
    void beenOnAnswer(AnswerType answerType);
}

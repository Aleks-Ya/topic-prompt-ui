package gptui.ui.model.question;

import gptui.ui.model.storage.AnswerType;
import gptui.ui.model.storage.InteractionId;

public interface QuestionModel {
    void requestAnswer(InteractionId interactionId, AnswerType answerType, Runnable callback);
}

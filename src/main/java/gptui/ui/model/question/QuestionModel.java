package gptui.ui.model.question;

import gptui.core.storagefilesystem.AnswerType;
import gptui.core.storagefilesystem.InteractionId;

public interface QuestionModel {
    void requestAnswer(InteractionId interactionId, AnswerType answerType, Runnable callback);

    void requestFollowUpAnswer(InteractionId interactionId, AnswerType answerType, Runnable callback);
}

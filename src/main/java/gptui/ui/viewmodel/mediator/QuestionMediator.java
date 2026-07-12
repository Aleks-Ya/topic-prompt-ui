package gptui.ui.viewmodel.mediator;

import gptui.core.storagefilesystem.AnswerType;
import gptui.core.storagefilesystem.Interaction;
import gptui.core.storagefilesystem.InteractionId;
import gptui.core.storagefilesystem.InteractionType;

import java.util.Optional;

public interface QuestionMediator {
    InteractionId getCurrentInteractionId();

    Optional<Interaction> getCurrentInteractionOpt();

    void setEditedQuestion(String question);

    Boolean isEnteringNewQuestion();

    void requestAnswer(InteractionId interactionId, AnswerType answerType);

    InteractionId createInteraction(InteractionType interactionType, InteractionId parentInteractionId);

    String getTextFromClipboard();
}

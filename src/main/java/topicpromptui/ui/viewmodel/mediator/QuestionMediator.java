package topicpromptui.ui.viewmodel.mediator;

import topicpromptui.core.storagefilesystem.AnswerType;
import topicpromptui.core.storagefilesystem.Interaction;
import topicpromptui.core.storagefilesystem.InteractionId;
import topicpromptui.core.storagefilesystem.InteractionType;

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

package topicpromptui.ui.viewmodel.mediator;

import topicpromptui.core.domain.AnswerType;
import topicpromptui.core.domain.Interaction;
import topicpromptui.core.domain.InteractionId;
import topicpromptui.core.domain.InteractionType;

import java.util.Optional;

public interface QuestionMediator {
    InteractionId getCurrentInteractionId();

    Optional<Interaction> getCurrentInteractionOpt();

    void setEditedQuestion(String question);

    Boolean isEnteringNewQuestion();

    void requestAnswer(InteractionId interactionId, AnswerType answerType);

    InteractionId createInteraction(InteractionType interactionType, InteractionId parentInteractionId);

    void setTopicComboBoxDisabled(boolean disabled);

    String getTextFromClipboard();
}

package gptui.ui.viewmodel.mediator;

import gptui.ui.model.storage.AnswerType;
import gptui.ui.model.storage.Interaction;
import gptui.ui.model.storage.InteractionId;
import gptui.ui.model.storage.InteractionType;

import java.util.Optional;

public interface QuestionMediator {
    InteractionId getCurrentInteractionId();

    Optional<Interaction> getCurrentInteractionOpt();

    void setEditedQuestion(String question);

    Boolean isEnteringNewQuestion();

    void requestAnswer(InteractionId interactionId, AnswerType answerType);

    InteractionId createInteraction(InteractionType interactionType);

    String getTextFromClipboard();
}

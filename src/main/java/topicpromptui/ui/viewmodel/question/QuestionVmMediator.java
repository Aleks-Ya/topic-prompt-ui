package topicpromptui.ui.viewmodel.question;

import topicpromptui.core.domain.InteractionType;

public interface QuestionVmMediator {
    void displayCurrentInteraction();

    void focusOnQuestionAndSelect();

    void pasteQuestionFromClipboard();

    void createNewInteractionAndRequestAnswers(InteractionType interactionType);

    void toggleFollowUp();
}

package gptui.ui.viewmodel.question;

import gptui.ui.model.storage.InteractionType;

public interface QuestionVmMediator {
    void displayCurrentInteraction();

    void focusOnQuestionAndSelect();

    void pasteQuestionFromClipboard();

    void createNewInteractionAndRequestAnswers(InteractionType interactionType);
}

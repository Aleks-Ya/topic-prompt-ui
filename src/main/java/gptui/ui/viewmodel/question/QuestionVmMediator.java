package gptui.ui.viewmodel.question;

import gptui.core.storagefilesystem.InteractionType;

public interface QuestionVmMediator {
    void displayCurrentInteraction();

    void focusOnQuestionAndSelect();

    void pasteQuestionFromClipboard();

    void createNewInteractionAndRequestAnswers(InteractionType interactionType);
}

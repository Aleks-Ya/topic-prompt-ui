package gptui.ui.viewmodel.mediator;

import gptui.core.storagefilesystem.Interaction;

import java.util.Optional;

public interface GptUiMediator {
    void displayCurrentInteraction();

    Optional<Interaction> getCurrentInteractionOpt();

    void chooseFirstInteractionAsCurrent();

    void chooseFirstThemeAsCurrent();
}

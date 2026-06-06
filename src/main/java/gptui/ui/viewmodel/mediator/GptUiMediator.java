package gptui.ui.viewmodel.mediator;

import gptui.core.storagefilesystem.AnswerType;
import gptui.core.storagefilesystem.Interaction;

import java.util.Optional;

public interface GptUiMediator {
    void displayCurrentInteraction();

    Optional<Interaction> getCurrentInteractionOpt();

    void setTemperature(AnswerType answerType, Integer temperature);

    void chooseFirstInteractionAsCurrent();

    void chooseFirstThemeAsCurrent();
}

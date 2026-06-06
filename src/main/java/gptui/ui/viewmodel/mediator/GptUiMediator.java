package gptui.ui.viewmodel.mediator;

import gptui.ui.model.storage.AnswerType;
import gptui.ui.model.storage.Interaction;

import java.util.Optional;

public interface GptUiMediator {
    void displayCurrentInteraction();

    Optional<Interaction> getCurrentInteractionOpt();

    void setTemperature(AnswerType answerType, Integer temperature);

    void chooseFirstInteractionAsCurrent();

    void chooseFirstThemeAsCurrent();
}

package gptui.ui.viewmodel.ui;

import gptui.ui.viewmodel.mediator.GptUiMediator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static gptui.core.storagefilesystem.AnswerType.GCP;
import static gptui.core.storagefilesystem.AnswerType.GRAMMAR;
import static gptui.core.storagefilesystem.AnswerType.LONG;
import static gptui.core.storagefilesystem.AnswerType.SHORT;

@Singleton
class GptUiVmImpl implements GptUiVmController {
    private static final Logger log = LoggerFactory.getLogger(GptUiVmImpl.class);
    @Inject
    private GptUiMediator mediator;

    @Override
    public void initialize() {
        log.trace("initialize");
        mediator.chooseFirstInteractionAsCurrent();
        mediator.getCurrentInteractionOpt().ifPresent(currentInteraction -> {
            currentInteraction.getAnswer(GRAMMAR).ifPresent(answer -> mediator.setTemperature(GRAMMAR, answer.temperature()));
            currentInteraction.getAnswer(SHORT).ifPresent(answer -> mediator.setTemperature(SHORT, answer.temperature()));
            currentInteraction.getAnswer(LONG).ifPresent(answer -> mediator.setTemperature(LONG, answer.temperature()));
            currentInteraction.getAnswer(GCP).ifPresent(answer -> mediator.setTemperature(GCP, answer.temperature()));
        });
        mediator.chooseFirstThemeAsCurrent();
        mediator.displayCurrentInteraction();
    }
}


package gptui.ui.viewmodel.ui;

import gptui.ui.viewmodel.mediator.GptUiMediator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static gptui.core.storagefilesystem.AnswerType.CLAUDE;
import static gptui.core.storagefilesystem.AnswerType.GCP;
import static gptui.core.storagefilesystem.AnswerType.GRAMMAR;
import static gptui.core.storagefilesystem.AnswerType.OPEN_AI;

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
            currentInteraction.getAnswer(OPEN_AI).ifPresent(answer -> mediator.setTemperature(OPEN_AI, answer.temperature()));
            currentInteraction.getAnswer(CLAUDE).ifPresent(answer -> mediator.setTemperature(CLAUDE, answer.temperature()));
            currentInteraction.getAnswer(GCP).ifPresent(answer -> mediator.setTemperature(GCP, answer.temperature()));
        });
        mediator.chooseFirstThemeAsCurrent();
        mediator.displayCurrentInteraction();
    }
}


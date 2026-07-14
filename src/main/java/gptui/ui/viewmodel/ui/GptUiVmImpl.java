package gptui.ui.viewmodel.ui;

import gptui.ui.viewmodel.mediator.GptUiMediator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class GptUiVmImpl implements GptUiVmController {
    private static final Logger log = LoggerFactory.getLogger(GptUiVmImpl.class);
    private final GptUiMediator mediator;

    @Inject
    GptUiVmImpl(GptUiMediator mediator) {
        this.mediator = mediator;
    }

    @Override
    public void initialize() {
        log.trace("initialize");
        mediator.chooseFirstInteractionAsCurrent();
        mediator.chooseFirstThemeAsCurrent();
        mediator.displayCurrentInteraction();
    }
}


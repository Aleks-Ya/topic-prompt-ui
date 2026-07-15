package topicpromptui.ui.viewmodel.ui;

import topicpromptui.ui.viewmodel.mediator.TopicPromptUiMediator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class TopicPromptUiVmImpl implements TopicPromptUiVmController {
    private static final Logger log = LoggerFactory.getLogger(TopicPromptUiVmImpl.class);
    private final TopicPromptUiMediator mediator;

    @Inject
    TopicPromptUiVmImpl(TopicPromptUiMediator mediator) {
        this.mediator = mediator;
    }

    @Override
    public void initialize() {
        log.trace("initialize");
        mediator.chooseFirstInteractionAsCurrent();
        mediator.chooseFirstTopicAsCurrent();
        mediator.displayCurrentInteraction();
    }
}


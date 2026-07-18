package topicpromptui.ui.viewmodel.ui;

import topicpromptui.core.storagefilesystem.AnswerType;
import topicpromptui.ui.viewmodel.mediator.TopicPromptUiMediator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class TopicPromptUiVmImpl implements TopicPromptUiVmController, TopicPromptUiVmMediator {
    private static final Logger log = LoggerFactory.getLogger(TopicPromptUiVmImpl.class);
    private final TopicPromptUiVmProperties properties = new TopicPromptUiVmProperties();
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

    @Override
    public TopicPromptUiVmProperties properties() {
        return properties;
    }

    @Override
    public void toggleExpandedAnswer(AnswerType answerType) {
        log.trace("toggleExpandedAnswer: {}", answerType);
        properties.expandedAnswerType.set(answerType.equals(properties.expandedAnswerType.get()) ? null : answerType);
    }

    @Override
    public boolean isAnswerExpanded() {
        return properties.expandedAnswerType.get() != null;
    }

    @Override
    public void collapseExpandedAnswer() {
        log.trace("collapseExpandedAnswer");
        properties.expandedAnswerType.set(null);
    }
}

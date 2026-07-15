package topicpromptui.ui.viewmodel.mediator;

import topicpromptui.core.storagefilesystem.Interaction;

import java.util.Optional;

public interface TopicPromptUiMediator {
    void displayCurrentInteraction();

    Optional<Interaction> getCurrentInteractionOpt();

    void chooseFirstInteractionAsCurrent();

    void chooseFirstTopicAsCurrent();
}

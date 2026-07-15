package gptui.ui.viewmodel.mediator;

import gptui.core.storagefilesystem.Interaction;
import gptui.core.storagefilesystem.InteractionId;
import gptui.core.storagefilesystem.Topic;
import gptui.core.storagefilesystem.TopicId;

import java.util.List;
import java.util.Optional;

public interface HistoryMediator {
    void displayCurrentInteraction();

    Interaction getCurrentInteraction();

    Optional<Interaction> getCurrentInteractionOpt();

    void setCurrentInteractionId(InteractionId currentInteractionId);

    void deleteCurrentInteraction();

    List<Interaction> getFullHistory();

    List<Interaction> getFilteredHistory();

    Topic getCurrentTopic();

    Topic getTopic(TopicId topicId);
}

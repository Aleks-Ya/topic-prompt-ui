package gptui.ui.model.storage;

import gptui.core.storagefilesystem.Interaction;
import gptui.core.storagefilesystem.InteractionId;
import gptui.core.storagefilesystem.Topic;
import gptui.core.storagefilesystem.TopicId;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

public interface StorageModel {
    InteractionId newInteractionId();

    void updateInteraction(InteractionId interactionId, UnaryOperator<Interaction> update);

    void saveInteraction(Interaction interaction);

    Optional<Interaction> readInteraction(InteractionId interactionId);

    List<Interaction> readAllInteractions();

    void deleteInteraction(InteractionId interactionId);

    List<Topic> getTopics();

    Topic addTopic(String topic);

    Topic renameTopic(TopicId topicId, String newTitle);

    void saveTopic(Topic topic);

    Topic getTopic(TopicId topicId);
}

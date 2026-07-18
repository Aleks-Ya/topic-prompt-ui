package topicpromptui.ui.model.storage;

import topicpromptui.core.storagefilesystem.Interaction;
import topicpromptui.core.storagefilesystem.InteractionId;
import topicpromptui.core.storagefilesystem.Topic;
import topicpromptui.core.storagefilesystem.TopicId;

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

    void deleteTopic(TopicId topicId);

    void saveTopic(Topic topic);

    Topic getTopic(TopicId topicId);
}

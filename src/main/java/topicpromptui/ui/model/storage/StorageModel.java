package topicpromptui.ui.model.storage;

import topicpromptui.core.domain.Interaction;
import topicpromptui.core.domain.InteractionId;
import topicpromptui.core.domain.Topic;
import topicpromptui.core.domain.TopicId;

import java.nio.file.Path;
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

    Path getInteractionFilePath(InteractionId interactionId);
}

package topicpromptui.core.storagefilesystem;

import java.util.List;

public interface StorageFilesystem {
    void saveInteraction(Interaction interaction);

    List<Interaction> readAllInteractions();

    void deleteInteraction(InteractionId interactionId);

    List<Topic> readTopics();

    void saveTopics(List<Topic> topics);
}

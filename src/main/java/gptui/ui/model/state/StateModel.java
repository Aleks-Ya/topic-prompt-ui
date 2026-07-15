package gptui.ui.model.state;

import gptui.core.storagefilesystem.Interaction;
import gptui.core.storagefilesystem.InteractionId;
import gptui.core.storagefilesystem.InteractionType;
import gptui.core.storagefilesystem.Topic;
import gptui.core.storagefilesystem.TopicId;

import java.util.List;
import java.util.Optional;

public interface StateModel {
    boolean isEnteringNewQuestion();

    List<Interaction> getFullHistory();

    List<Interaction> getFilteredHistory();

    InteractionId getCurrentInteractionId();

    Optional<Interaction> getCurrentInteractionOpt();

    void setCurrentInteractionId(InteractionId currentInteractionId);

    InteractionId createInteraction(InteractionType interactionType, InteractionId parentInteractionId);

    void deleteCurrentInteraction();

    List<Topic> getTopics();

    Topic addTopic(String topic);

    Topic renameTopic(TopicId topicId, String newTitle);

    Topic getTopic(TopicId topicId);

    Long getInteractionCountInTopic(String topic);

    Topic getCurrentTopic();

    void setCurrentTopic(Topic currentTopic);

    void setFirstTopicAsCurrent();

    String getEditedQuestion();

    void setEditedQuestion(String question);

    Boolean isHistoryFilteringEnabled();

    void setIsHistoryFilteringEnabled(Boolean isHistoryFilteringEnabled);

    void chooseFirstInteractionAsCurrent();
}

package topicpromptui.ui.model.state;

import topicpromptui.core.storagefilesystem.Interaction;
import topicpromptui.core.storagefilesystem.InteractionId;
import topicpromptui.core.storagefilesystem.InteractionType;
import topicpromptui.core.storagefilesystem.Topic;
import topicpromptui.core.storagefilesystem.TopicId;

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

    void deleteTopic(TopicId topicId);

    Topic getTopic(TopicId topicId);

    Long getInteractionCountInTopic(String topic);

    Topic getCurrentTopic();

    void setCurrentTopic(Topic currentTopic);

    void setFirstTopicAsCurrent();

    String getEditedQuestion();

    void setEditedQuestion(String question);

    Boolean isHistoryFilteringEnabled();

    void setIsHistoryFilteringEnabled(Boolean isHistoryFilteringEnabled);

    String getHistoryFilterText();

    void setHistoryFilterText(String historyFilterText);

    void chooseFirstInteractionAsCurrent();
}

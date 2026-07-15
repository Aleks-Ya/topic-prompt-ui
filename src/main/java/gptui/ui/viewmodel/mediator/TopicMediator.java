package gptui.ui.viewmodel.mediator;

import gptui.core.storagefilesystem.Interaction;
import gptui.core.storagefilesystem.Topic;
import gptui.core.storagefilesystem.TopicId;

import java.util.List;
import java.util.Optional;

public interface TopicMediator {
    void topicWasChosen();

    void isTopicFilterHistoryChanged();

    Boolean isHistoryFilteringEnabled();

    void setIsHistoryFilteringEnabled(Boolean isHistoryFilteringEnabled);

    void setCurrentTopic(Topic currentTopic);

    List<Topic> getTopics();

    Topic addTopic(String topic);

    Topic renameTopic(TopicId topicId, String newTitle);

    Long getInteractionCountInTopic(String topic);

    Optional<Interaction> getCurrentInteractionOpt();

    Topic getCurrentTopic();

    Topic getTopic(TopicId topicId);
}

package topicpromptui.ui.viewmodel.mediator;

import topicpromptui.core.domain.Interaction;
import topicpromptui.core.domain.Topic;
import topicpromptui.core.domain.TopicId;

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

    void deleteTopic(TopicId topicId);

    Long getInteractionCountInTopic(String topic);

    Optional<Interaction> getCurrentInteractionOpt();

    Topic getCurrentTopic();

    Topic getTopic(TopicId topicId);
}

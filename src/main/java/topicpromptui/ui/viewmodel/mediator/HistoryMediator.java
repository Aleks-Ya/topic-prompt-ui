package topicpromptui.ui.viewmodel.mediator;

import topicpromptui.core.domain.Interaction;
import topicpromptui.core.domain.InteractionId;
import topicpromptui.core.domain.Topic;
import topicpromptui.core.domain.TopicId;

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

    String getHistoryFilterText();

    void setHistoryFilterText(String filterText);
}

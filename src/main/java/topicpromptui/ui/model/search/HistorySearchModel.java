package topicpromptui.ui.model.search;

import topicpromptui.core.domain.Interaction;
import topicpromptui.core.domain.InteractionId;

import java.util.List;

public interface HistorySearchModel {
    List<InteractionId> search(String text);

    void indexDocuments(List<Interaction> interactions);

    void indexDocument(Interaction interaction);
}

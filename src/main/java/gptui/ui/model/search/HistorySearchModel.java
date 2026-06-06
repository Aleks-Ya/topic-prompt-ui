package gptui.ui.model.search;

import gptui.ui.model.storage.Interaction;
import gptui.ui.model.storage.InteractionId;

import java.util.List;

public interface HistorySearchModel {
    List<InteractionId> search(String text);

    void indexDocuments(List<Interaction> interactions);

    void indexDocument(Interaction interaction);
}

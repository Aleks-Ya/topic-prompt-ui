package gptui.ui.model.search;

import gptui.core.storagefilesystem.Interaction;
import gptui.core.storagefilesystem.InteractionId;

import java.util.List;

public interface HistorySearchModel {
    List<InteractionId> search(String text);

    void indexDocuments(List<Interaction> interactions);

    void indexDocument(Interaction interaction);
}

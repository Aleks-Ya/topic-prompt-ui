package gptui.ui.viewmodel.mediator;

import gptui.ui.model.storage.Interaction;
import gptui.ui.model.storage.InteractionId;
import gptui.ui.model.storage.Theme;
import gptui.ui.model.storage.ThemeId;

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

    Theme getCurrentTheme();

    Theme getTheme(ThemeId themeId);
}

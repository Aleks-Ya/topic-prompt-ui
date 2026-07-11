package gptui.ui.model.state;

import gptui.core.storagefilesystem.Interaction;
import gptui.core.storagefilesystem.InteractionId;
import gptui.core.storagefilesystem.InteractionType;
import gptui.core.storagefilesystem.Theme;
import gptui.core.storagefilesystem.ThemeId;

import java.util.List;
import java.util.Optional;

public interface StateModel {
    boolean isEnteringNewQuestion();

    List<Interaction> getFullHistory();

    List<Interaction> getFilteredHistory();

    InteractionId getCurrentInteractionId();

    Optional<Interaction> getCurrentInteractionOpt();

    void setCurrentInteractionId(InteractionId currentInteractionId);

    InteractionId createInteraction(InteractionType interactionType);

    void deleteCurrentInteraction();

    List<Theme> getThemes();

    Theme addTheme(String theme);

    Theme getTheme(ThemeId themeId);

    Long getInteractionCountInTheme(String theme);

    Theme getCurrentTheme();

    void setCurrentTheme(Theme currentTheme);

    void setFirstThemeAsCurrent();

    String getEditedQuestion();

    void setEditedQuestion(String question);

    Boolean isHistoryFilteringEnabled();

    void setIsHistoryFilteringEnabled(Boolean isHistoryFilteringEnabled);

    void chooseFirstInteractionAsCurrent();
}

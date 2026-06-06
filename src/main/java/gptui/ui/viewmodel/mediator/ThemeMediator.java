package gptui.ui.viewmodel.mediator;

import gptui.ui.model.storage.Interaction;
import gptui.ui.model.storage.Theme;
import gptui.ui.model.storage.ThemeId;

import java.util.List;
import java.util.Optional;

public interface ThemeMediator {
    void themeWasChosen();

    void isThemeFilterHistoryChanged();

    Boolean isHistoryFilteringEnabled();

    void setIsHistoryFilteringEnabled(Boolean isHistoryFilteringEnabled);

    void setCurrentTheme(Theme currentTheme);

    List<Theme> getThemes();

    Theme addTheme(String theme);

    Long getInteractionCountInTheme(String theme);

    Optional<Interaction> getCurrentInteractionOpt();

    Theme getCurrentTheme();

    Theme getTheme(ThemeId themeId);
}

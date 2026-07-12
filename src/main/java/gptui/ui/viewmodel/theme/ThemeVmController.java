package gptui.ui.viewmodel.theme;

public interface ThemeVmController {
    void onThemeComboBoxAction();

    void onThemeFilterHistoryCheckBoxClicked();

    void addNewTheme(String theme);

    void renameCurrentTheme(String newTitle);

    ThemeVmProperties properties();
}

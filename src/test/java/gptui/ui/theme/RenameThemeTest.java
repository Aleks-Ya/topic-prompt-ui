package gptui.ui.theme;

import gptui.BaseGptUiTest;
import gptui.core.storagefilesystem.Theme;
import gptui.ui.TestingData.I1;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;

import static gptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EMPTY;
import static javafx.scene.paint.Color.GREEN;
import static org.assertj.core.api.Assertions.assertThat;

class RenameThemeTest extends BaseGptUiTest {
    @Override
    public void init() {
        storage.saveTheme(I1.THEME);
        storage.saveInteraction(I1.INTERACTION);
    }

    @Test
    void renameTheme() {
        var renamedTheme = new Theme(I1.THEME_ID, "Renamed Theme 1");
        assertion()
                .focus(history().comboBox())
                .historySize(1, 1)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(I1.INTERACTION)
                .historyItems(I1.INTERACTION)
                .themeSize(1)
                .themeSelectedItem(I1.THEME)
                .themeItems(I1.THEME)
                .themeFilterHistorySelected(false)
                .themeRenameButtonDisabled(false)
                .questionText(I1.QUESTION)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .modelEditedQuestion(I1.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I1.GRAMMAR_HTML)
                .openAiA().text(I1.OPEN_AI_HTML)
                .claudeA().text(I1.CLAUDE_HTML)
                .gcpA().text(I1.GCP_HTML)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)

                .work("Rename Theme", () ->
                        clickOn(theme().renameButton()).write(renamedTheme.title()).type(KeyCode.ENTER))
                .focus(question().textArea())
                .themeSelectedItem(renamedTheme)
                .themeItems(renamedTheme)

                .work("Rename To Same Title Is No-op", () ->
                        clickOn(theme().renameButton()).type(KeyCode.ENTER))
                .themeSelectedItem(renamedTheme)
                .themeItems(renamedTheme)
                .assertApp();

        assertThat(storage.readInteraction(I1.INTERACTION.id()).orElseThrow().themeId()).isEqualTo(I1.THEME_ID);
    }
}

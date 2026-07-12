package gptui.ui.theme;

import gptui.BaseGptUiTest;
import gptui.ui.TestingData.I1;
import gptui.ui.TestingData.I2;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;

import static gptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EMPTY;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.RED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MergeThemeTest extends BaseGptUiTest {
    @Override
    public void init() {
        storage.saveTheme(I1.THEME);
        storage.saveTheme(I2.THEME);
        storage.saveInteraction(I1.INTERACTION);
        storage.saveInteraction(I2.INTERACTION);
    }

    @Test
    void mergeThemeOnNameCollision() {
        assertion()
                .focus(history().comboBox())
                .historySize(2, 2)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(I2.INTERACTION)
                .historyItems(I2.INTERACTION, I1.INTERACTION)
                .themeSize(2)
                .themeSelectedItem(I2.THEME)
                .themeItems(I2.THEME, I1.THEME)
                .themeFilterHistorySelected(false)
                .themeRenameButtonDisabled(false)
                .questionText(I2.QUESTION)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .modelEditedQuestion(I2.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I2.GRAMMAR_HTML)
                .openAiA().text(I2.OPEN_AI_HTML)
                .claudeA().text(I2.CLAUDE_HTML)
                .gcpA().text(I2.GCP_HTML)
                .answerCircleColors(GREEN, GREEN, RED, GREEN)

                .work("Rename Theme To Existing Title (merge)", () ->
                        clickOn(theme().renameButton()).write(I1.THEME.title()).type(KeyCode.ENTER))
                .focus(question().textArea())
                .themeSize(1)
                .themeSelectedItem(I1.THEME)
                .themeItems(I1.THEME)
                .historyItems(I2.INTERACTION.withThemeId(I1.THEME_ID), I1.INTERACTION)
                .assertApp();

        assertThat(storage.readInteraction(I2.INTERACTION.id()).orElseThrow().themeId()).isEqualTo(I1.THEME_ID);
        assertThatThrownBy(() -> storage.getTheme(I2.THEME_ID)).isInstanceOf(IllegalStateException.class);
    }
}

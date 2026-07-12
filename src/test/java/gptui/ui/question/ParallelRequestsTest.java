package gptui.ui.question;

import gptui.BaseGptUiTest;
import gptui.ui.TestingData.I0;
import gptui.ui.TestingData.I1;
import gptui.ui.TestingData.I2;
import org.junit.jupiter.api.Test;

import static gptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EDITED;
import static gptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EMPTY;
import static java.time.Duration.ofMillis;
import static javafx.scene.paint.Color.BLUE;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.WHITE;

class ParallelRequestsTest extends BaseGptUiTest {
    @Override
    public void init() {
        storage.saveTheme(I1.THEME);
        storage.saveTheme(I2.THEME);
    }

    @Test
    void shouldSendQuestion() {
        initialState();
        sendFirstQuestion();
        sendSecondQuestion();
        firstRequestFinished();
    }

    private void initialState() {
        gptApi.clear();
        assertion()
                .focus(history().comboBox())
                .historySize(0, 0)
                .historyDeleteButtonDisabled(true)
                .historySelectedItem(I0.HISTORY_SELECTED_ITEM)
                .historyItems(I0.HISTORY_ITEMS)
                .themeSize(2)
                .themeSelectedItem(I0.THEME_SELECTED_ITEM)
                .themeItems(I1.THEME, I2.THEME)
                .themeFilterHistorySelected(false)
                .themeRenameButtonDisabled(true)
                .questionText(I0.QUESTION)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .modelEditedQuestion(null)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I0.GRAMMAR_HTML)
                .openAiA().text(I0.OPEN_AI_HTML)
                .claudeA().text(I0.CLAUDE_HTML)
                .gcpA().text(I0.GCP_HTML)
                .answerCircleColors(WHITE, WHITE, WHITE, WHITE)
                .assertApp();
    }

    private void sendFirstQuestion() {
        clickOn(theme().comboBoxNarrow()).clickOn(I1.THEME.title() + " (0)");
        clickOn(question().textArea());
        overWrite(I1.QUESTION);
        assertion()
                .focus(question().textArea())
                .historySize(0, 0)
                .historyDeleteButtonDisabled(true)
                .historySelectedItem(I0.HISTORY_SELECTED_ITEM)
                .historyItems(I0.HISTORY_ITEMS)
                .themeSize(2)
                .themeSelectedItem(I1.THEME)
                .themeItems(I1.THEME, I2.THEME)
                .themeFilterHistorySelected(false)
                .themeRenameButtonDisabled(false)
                .questionText(I1.QUESTION)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .modelEditedQuestion(I1.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I0.GRAMMAR_HTML)
                .openAiA().text(I0.OPEN_AI_HTML)
                .claudeA().text(I0.CLAUDE_HTML)
                .gcpA().text(I0.GCP_HTML)
                .answerCircleColors(WHITE, WHITE, WHITE, WHITE)
                .assertApp();

        gptApi
                .putGrammarResponse(I1.GRAMMAR_HTML, ofMillis(10000))
                .putOpenAiResponse(I1.OPEN_AI_HTML, ofMillis(10500));
        claudeApi.putClaudeResponse(I1.CLAUDE_HTML, ofMillis(11000));
        gcpApi.putGcpResponse(I1.GCP_HTML, ofMillis(11500));

        clickOn(question().questionButton());
        assertion()
                .focus(question().questionButton())
                .historySize(1, 1)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(storage.readAllInteractions().getFirst())
                .historyItems(storage.readAllInteractions())
                .themeSize(2)
                .themeSelectedItem(I1.THEME)
                .themeItems(I1.THEME, I2.THEME)
                .themeFilterHistorySelected(false)
                .themeRenameButtonDisabled(false)
                .questionText(I1.QUESTION)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .modelEditedQuestion(I1.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I0.GRAMMAR_HTML)
                .openAiA().text(I0.OPEN_AI_HTML)
                .claudeA().text(I0.CLAUDE_HTML)
                .gcpA().text(I0.GCP_HTML)
                .answerCircleColors(BLUE, BLUE, BLUE, BLUE)
                .assertApp();
    }

    private void sendSecondQuestion() {
        clickOn(theme().comboBoxNarrow()).clickOn(I2.THEME.title() + " (0)");
        clickOn(question().textArea());
        overWrite(I2.QUESTION);
        assertion()
                .focus(question().textArea())
                .historySize(1, 1)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(storage.readAllInteractions().getFirst())
                .historyItems(storage.readAllInteractions())
                .themeSize(2)
                .themeSelectedItem(I2.THEME)
                .themeItems(I1.THEME, I2.THEME)
                .themeFilterHistorySelected(false)
                .themeRenameButtonDisabled(false)
                .questionText(I2.QUESTION)
                .questionStyle(QUESTION_STYLE_EDITED)
                .modelEditedQuestion(I2.QUESTION)
                .modelIsEnteringNewQuestion(true)
                .grammarA().text(I0.GRAMMAR_HTML)
                .openAiA().text(I0.OPEN_AI_HTML)
                .claudeA().text(I0.CLAUDE_HTML)
                .gcpA().text(I0.GCP_HTML)
                .answerCircleColors(BLUE, BLUE, BLUE, BLUE)
                .assertApp();

        gptApi
                .putGrammarResponse(I2.GRAMMAR_HTML, ofMillis(1000))
                .putOpenAiResponse(I2.OPEN_AI_HTML, ofMillis(1500));
        claudeApi.putClaudeResponse(I2.CLAUDE_HTML, ofMillis(2000));
        gcpApi.putGcpResponse(I2.GCP_HTML, ofMillis(2500));
        clickOn(question().questionButton());
        assertion()
                .focus(question().questionButton())
                .historySize(2, 2)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(storage.readAllInteractions().getFirst())
                .historyItems(storage.readAllInteractions())
                .themeSize(2)
                .themeSelectedItem(I2.THEME)
                .themeItems(I2.THEME, I1.THEME)
                .themeFilterHistorySelected(false)
                .themeRenameButtonDisabled(false)
                .questionText(I2.QUESTION)
                .questionStyle(QUESTION_STYLE_EDITED)
                .modelEditedQuestion(I2.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I0.GRAMMAR_HTML)
                .openAiA().text(I0.OPEN_AI_HTML)
                .claudeA().text(I0.CLAUDE_HTML)
                .gcpA().text(I0.GCP_HTML)
                .answerCircleColors(BLUE, BLUE, BLUE, BLUE)
                .assertApp();


        gptApi.waitUntilSent(2);
        claudeApi.waitUntilSent(1);
        gcpApi.waitUntilSent(1);
        assertion()
                .focus(question().questionButton())
                .historySize(2, 2)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(storage.readAllInteractions().getFirst())
                .historyItems(storage.readAllInteractions())
                .themeSize(2)
                .themeSelectedItem(I2.THEME)
                .themeItems(I2.THEME, I1.THEME)
                .themeFilterHistorySelected(false)
                .themeRenameButtonDisabled(false)
                .questionText(I2.QUESTION)
                .questionStyle(QUESTION_STYLE_EDITED)
                .modelEditedQuestion(I2.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I2.EXP_GRAMMAR_HTML_BODY)
                .openAiA().text(I2.EXP_OPEN_AI_HTML_BODY)
                .claudeA().text(I2.EXP_CLAUDE_HTML_BODY)
                .gcpA().text(I2.EXP_GCP_HTML_BODY)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)
                .assertApp();
    }

    private void firstRequestFinished() {
        gptApi.waitUntilSent(4);
        claudeApi.waitUntilSent(2);
        gcpApi.waitUntilSent(2);
        assertion()
                .focus(question().questionButton())
                .historySize(2, 2)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(storage.readAllInteractions().getFirst())
                .historyItems(storage.readAllInteractions())
                .themeSize(2)
                .themeSelectedItem(I2.THEME)
                .themeItems(I2.THEME, I1.THEME)
                .themeFilterHistorySelected(false)
                .themeRenameButtonDisabled(false)
                .questionText(I2.QUESTION)
                .questionStyle(QUESTION_STYLE_EDITED)
                .modelEditedQuestion(I2.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I2.EXP_GRAMMAR_HTML_BODY)
                .openAiA().text(I2.EXP_OPEN_AI_HTML_BODY)
                .claudeA().text(I2.EXP_CLAUDE_HTML_BODY)
                .gcpA().text(I2.EXP_GCP_HTML_BODY)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)
                .assertApp();
    }
}
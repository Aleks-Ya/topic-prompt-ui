package gptui.ui.question;

import gptui.BaseGptUiTest;
import gptui.ui.TestingData.I1;
import gptui.ui.TestingData.I2;
import gptui.ui.TestingData.I3;
import org.junit.jupiter.api.Test;

import static gptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EDITED;
import static gptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EMPTY;
import static java.time.Duration.ZERO;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.RED;

class SendFactTest extends BaseGptUiTest {
    @Override
    public void init() {
        storage.saveTheme(I1.THEME);
        storage.saveTheme(I2.THEME);
        storage.saveTheme(I3.THEME);
        storage.saveInteraction(I1.INTERACTION);
        storage.saveInteraction(I2.INTERACTION);
        storage.saveInteraction(I3.INTERACTION);
    }

    @Test
    void currentInteractionIsInMiddle() {
        assertion()
                .focus(history().comboBox())
                .historySize(3, 3)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(I3.INTERACTION)
                .historyItems(I3.INTERACTION, I2.INTERACTION, I1.INTERACTION)
                .themeSize(3)
                .themeSelectedItem(I3.THEME)
                .themeItems(I3.THEME, I2.THEME, I1.THEME)
                .themeFilterHistorySelected(false)
                .questionText(I3.QUESTION)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .modelEditedQuestion(I3.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I3.GRAMMAR_HTML)
                .openAiA().text(I3.OPEN_AI_HTML)
                .claudeA().text(I3.CLAUDE_HTML)
                .gcpA().text(I3.GCP_HTML)
                .answerCircleColors(GREEN, GREEN, RED, GREEN)
                .assertApp();

        gptApi.clear()
                .putGrammarResponse("Grammar answer 4", ZERO)
                .putFactResponse("Fact answer 4", ZERO);
        claudeApi.clear().putFactResponse("Fact answer 4", ZERO);
        gcpApi.clear().putFactResponse("Fact answer 4", ZERO);
        clickOn(question().textArea());
        overWrite("Question 4");
        clickOn(question().factButton());

        assertion()
                .focus(question().factButton())
                .historySize(4, 4)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(storage.readAllInteractions().getFirst())
                .historyItems(storage.readAllInteractions())
                .themeSize(3)
                .themeSelectedItem(I3.THEME)
                .themeItems(I3.THEME, I2.THEME, I1.THEME)
                .themeFilterHistorySelected(false)
                .questionText("Question 4")
                .questionStyle(QUESTION_STYLE_EDITED)
                .modelEditedQuestion("Question 4")
                .modelIsEnteringNewQuestion(false)
                .grammarA().text("<p>Grammar answer 4</p>\n")
                .openAiA().text("<p>Fact answer 4</p>\n")
                .claudeA().text("<p>Fact answer 4</p>\n")
                .gcpA().text("<p>Fact answer 4</p>\n")
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)
                .assertApp();
    }
}
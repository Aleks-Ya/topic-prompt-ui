package topicpromptui.ui.question;

import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.ui.TestingData.I0;
import topicpromptui.ui.TestingData.I1;
import org.junit.jupiter.api.Test;

import static topicpromptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EMPTY;
import static java.time.Duration.ZERO;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.WHITE;

class MultiLineQuestionTest extends BaseTopicPromptUiTest {
    @Override
    public void init() {
        storage.saveTopic(I1.TOPIC);
    }

    @Test
    void currentInteractionIsInMiddle() {
        initialState();
        sendQuestion();
    }

    private void initialState() {
        assertion()
                .focus(history().comboBox())
                .historySize(0, 0)
                .historyDeleteButtonDisabled(true)
                .historySelectedItem(I0.HISTORY_SELECTED_ITEM)
                .historyItems()
                .topicSize(1)
                .topicSelectedItem(I0.TOPIC_SELECTED_ITEM)
                .topicItems(I1.TOPIC)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(true)
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

    private void sendQuestion() {
        clickOn(topic().comboBoxNarrow()).clickOn(I1.TOPIC.title() + " (0)");
        clickOn(question().textArea());
        var questionLine1 = "Question line 1";
        var questionLine2 = "Question line 2";
        var questionLine3 = "Question line 3";
        overWrite(questionLine1).write("\n").write(questionLine2).write("\n").write(questionLine3);

        gptApi.clear()
                .putGrammarResponse(I1.GRAMMAR_HTML, ZERO)
                .putOpenAiResponse(I1.OPEN_AI_HTML, ZERO);
        claudeApi.clear().putClaudeResponse(I1.CLAUDE_HTML, ZERO);
        gcpApi.clear().putGcpResponse(I1.GCP_HTML, ZERO);
        clickOn(question().questionButton());

        gptApi.waitUntilSent(2);
        claudeApi.waitUntilSent(1);
        gcpApi.waitUntilSent(1);
        var questionText = questionLine1 + "\n" + questionLine2 + "\n" + questionLine3;
        assertion()
                .focus(question().questionButton())
                .historySize(1, 1)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(storage.readAllInteractions().getFirst())
                .historyItems(storage.readAllInteractions())
                .topicSize(1)
                .topicSelectedItem(I1.TOPIC)
                .topicItems(I1.TOPIC)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(false)
                .questionText(questionText)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .modelEditedQuestion(questionText)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I1.EXP_GRAMMAR_HTML_BODY)
                .openAiA().text(I1.EXP_OPEN_AI_HTML_BODY)
                .claudeA().text(I1.EXP_CLAUDE_HTML_BODY)
                .gcpA().text(I1.EXP_GCP_HTML_BODY)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)
                .assertApp();
    }
}
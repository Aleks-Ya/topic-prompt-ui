package topicpromptui.ui.question;

import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.ui.TestingData.I1;
import topicpromptui.ui.TestingData.I2;
import topicpromptui.core.storagefilesystem.Interaction;
import org.junit.jupiter.api.Test;

import static topicpromptui.core.storagefilesystem.AnswerState.FAIL;
import static topicpromptui.core.storagefilesystem.AnswerType.CLAUDE;
import static topicpromptui.core.storagefilesystem.AnswerType.GCP;
import static topicpromptui.core.storagefilesystem.AnswerType.OPEN_AI;
import static topicpromptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EMPTY;
import static java.time.Duration.ZERO;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.RED;

class RegenerateQuestionTest extends BaseTopicPromptUiTest {
    private final Interaction interaction1 = I1.INTERACTION
            .withAnswer(OPEN_AI, answer -> answer.withState(FAIL))
            .withAnswer(CLAUDE, answer -> answer.withState(FAIL))
            .withAnswer(GCP, answer -> answer.withState(FAIL));

    @Override
    public void init() {
        storage.saveTopic(I1.TOPIC);
        storage.saveInteraction(interaction1);
    }

    @Test
    void currentInteractionIsTheOnly() {
        assertion()
                .focus(history().comboBox())
                .historySize(1, 1)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(interaction1)
                .historyItems(interaction1)
                .topicSize(1)
                .topicSelectedItem(I1.TOPIC)
                .topicItems(I1.TOPIC)
                .topicFilterHistorySelected(false)
                .questionText(I1.QUESTION)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .modelEditedQuestion(I1.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I1.GRAMMAR_HTML)
                .openAiA().text(I1.OPEN_AI_HTML)
                .claudeA().text(I1.CLAUDE_HTML)
                .gcpA().text(I1.GCP_HTML)
                .answerCircleColors(GREEN, RED, RED, RED)
                .assertApp();

        gptApi.clear()
                .putGrammarResponse(I2.GRAMMAR_HTML, ZERO)
                .putOpenAiResponse(I2.OPEN_AI_HTML, ZERO);
        claudeApi.clear().putClaudeResponse(I2.CLAUDE_HTML, ZERO);
        gcpApi.clear().putGcpResponse(I2.GCP_HTML, ZERO);
        clickOn(question().regenerateButton());
        gptApi.waitUntilSent(2);
        claudeApi.waitUntilSent(1);
        gcpApi.waitUntilSent(1);

        assertion()
                .focus(question().regenerateButton())
                .historySize(1, 1)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(storage.readInteraction(interaction1.id()).orElseThrow())
                .historyItems(storage.readInteraction(interaction1.id()).orElseThrow())
                .topicSize(1)
                .topicSelectedItem(I1.TOPIC)
                .topicItems(I1.TOPIC)
                .topicFilterHistorySelected(false)
                .questionText(I1.QUESTION)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .modelEditedQuestion(I1.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I2.EXP_GRAMMAR_HTML_BODY)
                .openAiA().text(I2.EXP_OPEN_AI_HTML_BODY)
                .claudeA().text(I2.EXP_CLAUDE_HTML_BODY)
                .gcpA().text(I2.EXP_GCP_HTML_BODY)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)
                .assertApp();
    }
}
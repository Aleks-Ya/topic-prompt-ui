package topicpromptui.ui.question;

import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.ui.TestingData.I1;
import topicpromptui.ui.TestingData.I2;
import topicpromptui.core.domain.Interaction;
import org.junit.jupiter.api.Test;

import static topicpromptui.core.domain.AnswerState.FAIL;
import static topicpromptui.core.domain.AnswerType.AI_2;
import static topicpromptui.core.domain.AnswerType.AI_3;
import static topicpromptui.core.domain.AnswerType.AI_1;
import static topicpromptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EMPTY;
import static java.time.Duration.ZERO;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.RED;

class RegenerateQuestionUiTest extends BaseTopicPromptUiTest {
    private final Interaction interaction1 = I1.INTERACTION
            .withAnswer(AI_1, answer -> answer.withState(FAIL))
            .withAnswer(AI_2, answer -> answer.withState(FAIL))
            .withAnswer(AI_3, answer -> answer.withState(FAIL));

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
                .ai1A().text(I1.AI_1_HTML)
                .ai2A().text(I1.AI_2_HTML)
                .ai3A().text(I1.AI_3_HTML)
                .answerCircleColors(GREEN, RED, RED, RED)
                .assertApp();

        gptApi.clear()
                .putGrammarResponse(I1.GRAMMAR_ANSWER, ZERO)
                .putOpenAiResponse(I2.AI_1_HTML, ZERO);
        xaiApi.clear().putGrokResponse(I2.AI_2_HTML, ZERO);
        gcpApi.clear().putGcpResponse(I2.AI_3_HTML, ZERO);
        clickOn(question().regenerateButton());
        gptApi.waitUntilSent(2);
        xaiApi.waitUntilSent(1);
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
                .grammarA().text(I1.EXP_GRAMMAR_ANSWER_BODY)
                .ai1A().text(I2.EXP_AI_1_HTML_BODY)
                .ai2A().text(I2.EXP_AI_2_HTML_BODY)
                .ai3A().text(I2.EXP_AI_3_HTML_BODY)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)
                .assertApp();
    }
}
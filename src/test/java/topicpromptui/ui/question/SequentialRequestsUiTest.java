package topicpromptui.ui.question;

import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.ui.TestingData.I0;
import topicpromptui.ui.TestingData.I1;
import topicpromptui.ui.TestingData.I2;
import org.junit.jupiter.api.Test;

import static topicpromptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EDITED;
import static topicpromptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EMPTY;
import static java.time.Duration.ofMillis;
import static javafx.scene.paint.Color.BLUE;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.WHITE;

class SequentialRequestsUiTest extends BaseTopicPromptUiTest {
    @Override
    public void init() {
        storage.saveTopic(I1.TOPIC);
        storage.saveTopic(I2.TOPIC);
    }

    @Test
    void shouldSendQuestion() {
        initialState();
        sendFirstQuestion();
        sendSecondQuestion();
        choosePreviousInteraction();
    }

    private void initialState() {
        assertion()
                .focus(history().comboBox())
                .historySize(0, 0)
                .historyDeleteButtonDisabled(true)
                .historySelectedItem(I0.HISTORY_SELECTED_ITEM)
                .historyItems(I0.HISTORY_ITEMS)
                .topicSize(2)
                .topicSelectedItem(I0.TOPIC_SELECTED_ITEM)
                .topicItems(I1.TOPIC, I2.TOPIC)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(true)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .questionText(I0.QUESTION)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .modelEditedQuestion(null)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I0.GRAMMAR_HTML)
                .ai1A().text(I0.AI_1_HTML)
                .ai2A().text(I0.AI_2_HTML)
                .ai3A().text(I0.AI_3_HTML)
                .answerCircleColors(WHITE, WHITE, WHITE, WHITE)
                .assertApp();
    }

    private void sendFirstQuestion() {
        clickOn(topic().comboBoxNarrow()).clickOn(I1.TOPIC.title() + " (0)");
        clickOn(question().textArea());
        overWrite(I1.QUESTION);
        assertion()
                .focus(question().textArea())
                .historySize(0, 0)
                .historyDeleteButtonDisabled(true)
                .historySelectedItem(I0.HISTORY_SELECTED_ITEM)
                .historyItems(I0.HISTORY_ITEMS)
                .topicSize(2)
                .topicSelectedItem(I1.TOPIC)
                .topicItems(I1.TOPIC, I2.TOPIC)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(false)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .questionText(I1.QUESTION)
                .modelEditedQuestion(I1.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I0.GRAMMAR_HTML)
                .ai1A().text(I0.AI_1_HTML)
                .ai2A().text(I0.AI_2_HTML)
                .ai3A().text(I0.AI_3_HTML)
                .answerCircleColors(WHITE, WHITE, WHITE, WHITE)
                .assertApp();

        gptApi.clear()
                .putGrammarResponse(I1.GRAMMAR_ANSWER, ofMillis(1000))
                .putOpenAiResponse(I1.AI_1_HTML, ofMillis(1500));
        xaiApi.clear().putGrokResponse(I1.AI_2_HTML, ofMillis(2000));
        gcpApi.clear().putGcpResponse(I1.AI_3_HTML, ofMillis(2500));

        clickOn(question().questionButton());
        assertion()
                .focus(question().questionButton())
                .historySize(1, 1)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(storage.readAllInteractions().getFirst())
                .historyItems(storage.readAllInteractions())
                .topicSize(2)
                .topicSelectedItem(I1.TOPIC)
                .topicItems(I1.TOPIC, I2.TOPIC)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(false)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .questionText(I1.QUESTION)
                .modelEditedQuestion(I1.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I0.GRAMMAR_HTML)
                .ai1A().text(I0.AI_1_HTML)
                .ai2A().text(I0.AI_2_HTML)
                .ai3A().text(I0.AI_3_HTML)
                .answerCircleColors(BLUE, BLUE, BLUE, BLUE)
                .assertApp();

        gptApi.waitUntilSent(2);
        xaiApi.waitUntilSent(1);
        gcpApi.waitUntilSent(1);
        assertion()
                .focus(question().questionButton())
                .historySize(1, 1)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(storage.readAllInteractions().getFirst())
                .historyItems(storage.readAllInteractions())
                .topicSize(2)
                .topicSelectedItem(I1.TOPIC)
                .topicItems(I1.TOPIC, I2.TOPIC)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(false)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .questionText(I1.QUESTION)
                .modelEditedQuestion(I1.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I1.EXP_GRAMMAR_ANSWER_BODY)
                .ai1A().text(I1.EXP_AI_1_HTML_BODY)
                .ai2A().text(I1.EXP_AI_2_HTML_BODY)
                .ai3A().text(I1.EXP_AI_3_HTML_BODY)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)
                .assertApp();

        clickOn(ai1Answer().copyButton());
        assertion()
                .focus(ai1Answer().copyButton())
                .historySize(1, 1)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(storage.readAllInteractions().getFirst())
                .historyItems(storage.readAllInteractions())
                .topicSize(2)
                .topicSelectedItem(I1.TOPIC)
                .topicItems(I1.TOPIC, I2.TOPIC)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(false)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .questionText(I1.QUESTION)
                .modelEditedQuestion(I1.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I1.EXP_GRAMMAR_ANSWER_BODY)
                .ai1A().text(I1.EXP_AI_1_HTML_BODY)
                .ai2A().text(I1.EXP_AI_2_HTML_BODY)
                .ai3A().text(I1.EXP_AI_3_HTML_BODY)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)
                .clipboard(I1.EXP_AI_1_HTML_BODY)
                .assertApp();

        clickOn(ai2Answer().copyButton());
        assertion()
                .focus(ai2Answer().copyButton())
                .historySize(1, 1)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(storage.readAllInteractions().getFirst())
                .historyItems(storage.readAllInteractions())
                .topicSize(2)
                .topicSelectedItem(I1.TOPIC)
                .topicItems(I1.TOPIC, I2.TOPIC)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(false)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .questionText(I1.QUESTION)
                .modelEditedQuestion(I1.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I1.EXP_GRAMMAR_ANSWER_BODY)
                .ai1A().text(I1.EXP_AI_1_HTML_BODY)
                .ai2A().text(I1.EXP_AI_2_HTML_BODY)
                .ai3A().text(I1.EXP_AI_3_HTML_BODY)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)
                .clipboard(I1.EXP_AI_2_HTML_BODY)
                .assertApp();

        clickOn(ai3Answer().copyButton());
        assertion()
                .focus(ai3Answer().copyButton())
                .historySize(1, 1)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(storage.readAllInteractions().getFirst())
                .historyItems(storage.readAllInteractions())
                .topicSize(2)
                .topicSelectedItem(I1.TOPIC)
                .topicItems(I1.TOPIC, I2.TOPIC)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(false)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .questionText(I1.QUESTION)
                .modelEditedQuestion(I1.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I1.EXP_GRAMMAR_ANSWER_BODY)
                .ai1A().text(I1.EXP_AI_1_HTML_BODY)
                .ai2A().text(I1.EXP_AI_2_HTML_BODY)
                .ai3A().text(I1.EXP_AI_3_HTML_BODY)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)
                .clipboard(I1.EXP_AI_3_HTML_BODY)
                .assertApp();
    }

    private void sendSecondQuestion() {
        clickOn(topic().comboBoxNarrow()).clickOn(I2.TOPIC.title() + " (0)");
        clickOn(question().textArea());
        overWrite(I2.QUESTION);
        assertion()
                .focus(question().textArea())
                .historySize(1, 1)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(storage.readAllInteractions().getFirst())
                .historyItems(storage.readAllInteractions())
                .topicSize(2)
                .topicSelectedItem(I2.TOPIC)
                .topicItems(I1.TOPIC, I2.TOPIC)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(false)
                .questionStyle(QUESTION_STYLE_EDITED)
                .questionText(I2.QUESTION)
                .modelEditedQuestion(I2.QUESTION)
                .modelIsEnteringNewQuestion(true)
                .grammarA().text(I1.EXP_GRAMMAR_ANSWER_BODY)
                .ai1A().text(I1.EXP_AI_1_HTML_BODY)
                .ai2A().text(I1.EXP_AI_2_HTML_BODY)
                .ai3A().text(I1.EXP_AI_3_HTML_BODY)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)
                .assertApp();

        gptApi.clear()
                .putGrammarResponse(I2.GRAMMAR_ANSWER, ofMillis(1000))
                .putOpenAiResponse(I2.AI_1_HTML, ofMillis(1500));
        xaiApi.clear().putGrokResponse(I2.AI_2_HTML, ofMillis(2000));
        gcpApi.clear().putGcpResponse(I2.AI_3_HTML, ofMillis(2500));
        clickOn(question().questionButton());
        assertion()
                .focus(question().questionButton())
                .historySize(2, 2)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(storage.readAllInteractions().getFirst())
                .historyItems(storage.readAllInteractions())
                .topicSize(2)
                .topicSelectedItem(I2.TOPIC)
                .topicItems(I2.TOPIC, I1.TOPIC)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(false)
                .questionStyle(QUESTION_STYLE_EDITED)
                .questionText(I2.QUESTION)
                .modelEditedQuestion(I2.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I0.GRAMMAR_HTML)
                .ai1A().text(I0.AI_1_HTML)
                .ai2A().text(I0.AI_2_HTML)
                .ai3A().text(I0.AI_3_HTML)
                .answerCircleColors(BLUE, BLUE, BLUE, BLUE)
                .assertApp();


        gptApi.waitUntilSent(2);
        xaiApi.waitUntilSent(1);
        gcpApi.waitUntilSent(1);
        assertion()
                .focus(question().questionButton())
                .historySize(2, 2)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(storage.readAllInteractions().getFirst())
                .historyItems(storage.readAllInteractions())
                .topicSize(2)
                .topicSelectedItem(I2.TOPIC)
                .topicItems(I2.TOPIC, I1.TOPIC)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(false)
                .questionStyle(QUESTION_STYLE_EDITED)
                .questionText(I2.QUESTION)
                .modelEditedQuestion(I2.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I2.EXP_GRAMMAR_ANSWER_BODY)
                .ai1A().text(I2.EXP_AI_1_HTML_BODY)
                .ai2A().text(I2.EXP_AI_2_HTML_BODY)
                .ai3A().text(I2.EXP_AI_3_HTML_BODY)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)
                .assertApp();

        clickOn(ai1Answer().copyButton());
        assertion()
                .focus(ai1Answer().copyButton())
                .historySize(2, 2)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(storage.readAllInteractions().getFirst())
                .historyItems(storage.readAllInteractions())
                .topicSize(2)
                .topicSelectedItem(I2.TOPIC)
                .topicItems(I2.TOPIC, I1.TOPIC)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(false)
                .questionStyle(QUESTION_STYLE_EDITED)
                .questionText(I2.QUESTION)
                .modelEditedQuestion(I2.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I2.EXP_GRAMMAR_ANSWER_BODY)
                .ai1A().text(I2.EXP_AI_1_HTML_BODY)
                .ai2A().text(I2.EXP_AI_2_HTML_BODY)
                .ai3A().text(I2.EXP_AI_3_HTML_BODY)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)
                .clipboard(I2.EXP_AI_1_HTML_BODY)
                .assertApp();

        clickOn(ai2Answer().copyButton());
        assertion()
                .focus(ai2Answer().copyButton())
                .historySize(2, 2)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(storage.readAllInteractions().getFirst())
                .historyItems(storage.readAllInteractions())
                .topicSize(2)
                .topicSelectedItem(I2.TOPIC)
                .topicItems(I2.TOPIC, I1.TOPIC)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(false)
                .questionStyle(QUESTION_STYLE_EDITED)
                .questionText(I2.QUESTION)
                .modelEditedQuestion(I2.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I2.EXP_GRAMMAR_ANSWER_BODY)
                .ai1A().text(I2.EXP_AI_1_HTML_BODY)
                .ai2A().text(I2.EXP_AI_2_HTML_BODY)
                .ai3A().text(I2.EXP_AI_3_HTML_BODY)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)
                .clipboard(I2.EXP_AI_2_HTML_BODY)
                .assertApp();

        clickOn(ai3Answer().copyButton());
        assertion()
                .focus(ai3Answer().copyButton())
                .historySize(2, 2)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(storage.readAllInteractions().getFirst())
                .historyItems(storage.readAllInteractions())
                .topicSize(2)
                .topicSelectedItem(I2.TOPIC)
                .topicItems(I2.TOPIC, I1.TOPIC)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(false)
                .questionStyle(QUESTION_STYLE_EDITED)
                .questionText(I2.QUESTION)
                .modelEditedQuestion(I2.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I2.EXP_GRAMMAR_ANSWER_BODY)
                .ai1A().text(I2.EXP_AI_1_HTML_BODY)
                .ai2A().text(I2.EXP_AI_2_HTML_BODY)
                .ai3A().text(I2.EXP_AI_3_HTML_BODY)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)
                .clipboard(I2.EXP_AI_3_HTML_BODY)
                .assertApp();
    }

    private void choosePreviousInteraction() {
        assertion()
                .focus(ai3Answer().copyButton())
                .historySize(2, 2)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(storage.readAllInteractions().getFirst())
                .historyItems(storage.readAllInteractions())
                .topicSize(2)
                .topicSelectedItem(I2.TOPIC)
                .topicItems(I2.TOPIC, I1.TOPIC)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(false)
                .questionText(I2.QUESTION)
                .questionStyle(QUESTION_STYLE_EDITED)
                .modelEditedQuestion(I2.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I2.EXP_GRAMMAR_ANSWER_BODY)
                .ai1A().text(I2.EXP_AI_1_HTML_BODY)
                .ai2A().text(I2.EXP_AI_2_HTML_BODY)
                .ai3A().text(I2.EXP_AI_3_HTML_BODY)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)
                .assertApp();

        clickOn(history().comboBox()).clickOn(String.format("[Q] %s: %s", I1.TOPIC.title(), I1.QUESTION));
        assertion()
                .focus(history().comboBox())
                .historySize(2, 2)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(storage.readAllInteractions().get(1))
                .historyItems(storage.readAllInteractions())
                .topicSize(2)
                .topicSelectedItem(I1.TOPIC)
                .topicItems(I2.TOPIC, I1.TOPIC)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(false)
                .questionText(I1.QUESTION)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .modelEditedQuestion(I1.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I1.EXP_GRAMMAR_ANSWER_BODY)
                .ai1A().text(I1.EXP_AI_1_HTML_BODY)
                .ai2A().text(I1.EXP_AI_2_HTML_BODY)
                .ai3A().text(I1.EXP_AI_3_HTML_BODY)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)
                .assertApp();

        clickOn(ai1Answer().copyButton());
        assertion()
                .focus(ai1Answer().copyButton())
                .historySize(2, 2)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(storage.readAllInteractions().get(1))
                .historyItems(storage.readAllInteractions())
                .topicSize(2)
                .topicSelectedItem(I1.TOPIC)
                .topicItems(I2.TOPIC, I1.TOPIC)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(false)
                .questionText(I1.QUESTION)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .modelEditedQuestion(I1.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I1.EXP_GRAMMAR_ANSWER_BODY)
                .ai1A().text(I1.EXP_AI_1_HTML_BODY)
                .ai2A().text(I1.EXP_AI_2_HTML_BODY)
                .ai3A().text(I1.EXP_AI_3_HTML_BODY)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)
                .clipboard(I1.EXP_AI_1_HTML_BODY)
                .assertApp();

        clickOn(ai2Answer().copyButton());
        assertion()
                .focus(ai2Answer().copyButton())
                .historySize(2, 2)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(storage.readAllInteractions().get(1))
                .historyItems(storage.readAllInteractions())
                .topicSize(2)
                .topicSelectedItem(I1.TOPIC)
                .topicItems(I2.TOPIC, I1.TOPIC)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(false)
                .questionText(I1.QUESTION)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .modelEditedQuestion(I1.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I1.EXP_GRAMMAR_ANSWER_BODY)
                .ai1A().text(I1.EXP_AI_1_HTML_BODY)
                .ai2A().text(I1.EXP_AI_2_HTML_BODY)
                .ai3A().text(I1.EXP_AI_3_HTML_BODY)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)
                .clipboard(I1.EXP_AI_2_HTML_BODY)
                .assertApp();

        clickOn(ai3Answer().copyButton());
        assertion()
                .focus(ai3Answer().copyButton())
                .historySize(2, 2)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(storage.readAllInteractions().get(1))
                .historyItems(storage.readAllInteractions())
                .topicSize(2)
                .topicSelectedItem(I1.TOPIC)
                .topicItems(I2.TOPIC, I1.TOPIC)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(false)
                .questionText(I1.QUESTION)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .modelEditedQuestion(I1.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I1.EXP_GRAMMAR_ANSWER_BODY)
                .ai1A().text(I1.EXP_AI_1_HTML_BODY)
                .ai2A().text(I1.EXP_AI_2_HTML_BODY)
                .ai3A().text(I1.EXP_AI_3_HTML_BODY)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)
                .clipboard(I1.EXP_AI_3_HTML_BODY)
                .assertApp();
    }
}
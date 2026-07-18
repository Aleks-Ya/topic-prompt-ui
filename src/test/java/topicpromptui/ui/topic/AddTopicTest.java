package topicpromptui.ui.topic;

import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.ui.TestingData.I0;
import topicpromptui.ui.TestingData.I1;
import topicpromptui.ui.TestingData.I2;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;

import static topicpromptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EMPTY;
import static javafx.scene.paint.Color.WHITE;

class AddTopicTest extends BaseTopicPromptUiTest {
    @Test
    void addTopics() {
        assertion()
                .focus(history().comboBox())
                .historySize(0, 0)
                .historyDeleteButtonDisabled(true)
                .historySelectedItem(I0.HISTORY_SELECTED_ITEM)
                .historyItems(I0.HISTORY_ITEMS)
                .topicSize(I0.TOPIC_SIZE)
                .topicSelectedItem(I0.TOPIC_SELECTED_ITEM)
                .topicItems(I0.TOPIC_ITEMS)
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

                .work("Add Topic 1", () ->
                        clickOn(topic().addTopicButton()).write(I1.TOPIC.title()).type(KeyCode.ENTER))
                .focus(question().textArea())
                .topicSize(1)
                .topicSelectedItem(I1.TOPIC)
                .topicItems(I1.TOPIC)
                .topicRenameButtonDisabled(false)

                .work("Add Topic 2", () ->
                        clickOn(topic().addTopicButton()).write(I2.TOPIC.title()).type(KeyCode.ENTER))
                .topicSize(2)
                .topicSelectedItem(I2.TOPIC)
                .topicItems(I1.TOPIC, I2.TOPIC)

                .work("Add Duplicating Topic", () ->
                        clickOn(topic().addTopicButton()).write(I2.TOPIC.title()).type(KeyCode.ENTER))
                .assertApp();
    }

    @Test
    void blankTopicNameDoesNotSubmit() {
        assertion()
                .focus(history().comboBox())
                .historySize(0, 0)
                .historyDeleteButtonDisabled(true)
                .historySelectedItem(I0.HISTORY_SELECTED_ITEM)
                .historyItems(I0.HISTORY_ITEMS)
                .topicSize(I0.TOPIC_SIZE)
                .topicSelectedItem(I0.TOPIC_SELECTED_ITEM)
                .topicItems(I0.TOPIC_ITEMS)
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

                // Whitespace-only input must leave the OK button disabled, so ENTER does not submit
                // and the topic list stays untouched.
                .work("Try add blank topic", () ->
                        clickOn(topic().addTopicButton()).write("   ").type(KeyCode.ENTER))
                .focus(topic().addTopicButton())
                .topicSize(0)
                .topicItems()

                .work("Cancel dialog", () -> type(KeyCode.ESCAPE))
                .assertApp();
    }
}

package topicpromptui.ui.topic;

import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.ui.TestingData.I0;
import topicpromptui.ui.TestingData.I1;
import topicpromptui.ui.TestingData.I2;
import javafx.scene.control.DialogPane;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;

import static topicpromptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EMPTY;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.RED;
import static javafx.scene.paint.Color.WHITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeleteTopicTest extends BaseTopicPromptUiTest {
    @Override
    public void init() {
        storage.saveTopic(I1.TOPIC);
        storage.saveTopic(I2.TOPIC);
        storage.saveInteraction(I1.INTERACTION);
        storage.saveInteraction(I2.INTERACTION);
    }

    @Test
    void deleteTopicWithItsInteractions() {
        assertion()
                .focus(history().comboBox())
                .historySize(2, 2)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(I2.INTERACTION)
                .historyItems(I2.INTERACTION, I1.INTERACTION)
                .topicSize(2)
                .topicSelectedItem(I2.TOPIC)
                .topicItems(I2.TOPIC, I1.TOPIC)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(false)
                .questionText(I2.QUESTION)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .modelEditedQuestion(I2.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I2.GRAMMAR_HTML)
                .openAiA().text(I2.OPEN_AI_HTML)
                .claudeA().text(I2.CLAUDE_HTML)
                .gcpA().text(I2.GCP_HTML)
                .answerCircleColors(GREEN, GREEN, RED, GREEN)

                .work("Delete Topic 2 (confirm)", () -> {
                    clickOn(topic().deleteButton());
                    var dialogPane = lookup(".dialog-pane").queryAs(DialogPane.class);
                    assertThat(dialogPane.getHeaderText()).isEqualTo("Delete topic \"" + I2.TOPIC.title() + "\"?");
                    assertThat(dialogPane.getContentText())
                            .isEqualTo("This will also delete 1 interaction(s) in this topic.");
                    clickOn("OK");
                })
                .focus(question().textArea())
                .historySize(1, 1)
                .historySelectedItem(I1.INTERACTION)
                .historyItems(I1.INTERACTION)
                .topicSize(1)
                .topicSelectedItem(I1.TOPIC)
                .topicItems(I1.TOPIC)
                .questionText(I1.QUESTION)
                .modelEditedQuestion(I1.QUESTION)
                .grammarA().text(I1.GRAMMAR_HTML)
                .openAiA().text(I1.OPEN_AI_HTML)
                .claudeA().text(I1.CLAUDE_HTML)
                .gcpA().text(I1.GCP_HTML)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)
                .assertApp();

        assertThatThrownBy(() -> storage.getTopic(I2.TOPIC_ID)).isInstanceOf(IllegalStateException.class);
        assertThat(storage.readInteraction(I2.INTERACTION.id())).isEmpty();
        assertThat(storage.readInteraction(I1.INTERACTION.id())).isPresent();
    }

    @Test
    void cancelKeepsTopicAndInteractions() {
        assertion()
                .focus(history().comboBox())
                .historySize(2, 2)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(I2.INTERACTION)
                .historyItems(I2.INTERACTION, I1.INTERACTION)
                .topicSize(2)
                .topicSelectedItem(I2.TOPIC)
                .topicItems(I2.TOPIC, I1.TOPIC)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(false)
                .questionText(I2.QUESTION)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .modelEditedQuestion(I2.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I2.GRAMMAR_HTML)
                .openAiA().text(I2.OPEN_AI_HTML)
                .claudeA().text(I2.CLAUDE_HTML)
                .gcpA().text(I2.GCP_HTML)
                .answerCircleColors(GREEN, GREEN, RED, GREEN)

                // Cancel is the default button, so ENTER must dismiss the dialog without deleting.
                .work("Cancel deletion with ENTER", () ->
                        clickOn(topic().deleteButton()).type(KeyCode.ENTER))
                .focus(topic().deleteButton())

                .work("Cancel deletion with ESCAPE", () ->
                        clickOn(topic().deleteButton()).type(KeyCode.ESCAPE))
                .assertApp();

        assertThat(storage.getTopic(I2.TOPIC_ID)).isEqualTo(I2.TOPIC);
        assertThat(storage.readInteraction(I2.INTERACTION.id())).isPresent();
        assertThat(storage.readInteraction(I1.INTERACTION.id())).isPresent();
    }

    @Test
    void deleteAllTopics() {
        clickOn(topic().deleteButton());
        clickOn("OK");
        clickOn(topic().deleteButton());
        clickOn("OK");

        assertion()
                .focus(question().textArea())
                .historySize(0, 0)
                .historyDeleteButtonDisabled(true)
                .historySelectedItem(I0.HISTORY_SELECTED_ITEM)
                .historyItems(I0.HISTORY_ITEMS)
                .topicSize(0)
                .topicSelectedItem(I0.TOPIC_SELECTED_ITEM)
                .topicItems(I0.TOPIC_ITEMS)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(true)
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

        assertThat(storage.getTopics()).isEmpty();
        assertThat(storage.readAllInteractions()).isEmpty();
    }
}

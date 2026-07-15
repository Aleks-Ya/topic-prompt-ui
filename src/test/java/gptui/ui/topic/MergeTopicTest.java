package gptui.ui.topic;

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

class MergeTopicTest extends BaseGptUiTest {
    @Override
    public void init() {
        storage.saveTopic(I1.TOPIC);
        storage.saveTopic(I2.TOPIC);
        storage.saveInteraction(I1.INTERACTION);
        storage.saveInteraction(I2.INTERACTION);
    }

    @Test
    void mergeTopicOnNameCollision() {
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

                .work("Rename Topic To Existing Title (merge)", () ->
                        clickOn(topic().renameButton()).write(I1.TOPIC.title()).type(KeyCode.ENTER))
                .focus(question().textArea())
                .topicSize(1)
                .topicSelectedItem(I1.TOPIC)
                .topicItems(I1.TOPIC)
                .historyItems(I2.INTERACTION.withTopicId(I1.TOPIC_ID), I1.INTERACTION)
                .assertApp();

        assertThat(storage.readInteraction(I2.INTERACTION.id()).orElseThrow().topicId()).isEqualTo(I1.TOPIC_ID);
        assertThatThrownBy(() -> storage.getTopic(I2.TOPIC_ID)).isInstanceOf(IllegalStateException.class);
    }
}

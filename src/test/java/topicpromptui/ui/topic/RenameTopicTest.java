package topicpromptui.ui.topic;

import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.core.storagefilesystem.Topic;
import topicpromptui.ui.TestingData.I1;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;

import static topicpromptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EMPTY;
import static javafx.scene.paint.Color.GREEN;
import static org.assertj.core.api.Assertions.assertThat;

class RenameTopicTest extends BaseTopicPromptUiTest {
    @Override
    public void init() {
        storage.saveTopic(I1.TOPIC);
        storage.saveInteraction(I1.INTERACTION);
    }

    @Test
    void renameTopic() {
        var renamedTopic = new Topic(I1.TOPIC_ID, "Renamed Topic 1");
        assertion()
                .focus(history().comboBox())
                .historySize(1, 1)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(I1.INTERACTION)
                .historyItems(I1.INTERACTION)
                .topicSize(1)
                .topicSelectedItem(I1.TOPIC)
                .topicItems(I1.TOPIC)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(false)
                .questionText(I1.QUESTION)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .modelEditedQuestion(I1.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I1.GRAMMAR_HTML)
                .openAiA().text(I1.OPEN_AI_HTML)
                .claudeA().text(I1.CLAUDE_HTML)
                .gcpA().text(I1.GCP_HTML)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)

                .work("Rename Topic", () ->
                        clickOn(topic().renameButton()).write(renamedTopic.title()).type(KeyCode.ENTER))
                .focus(question().textArea())
                .topicSelectedItem(renamedTopic)
                .topicItems(renamedTopic)

                .work("Rename To Same Title Is No-op", () ->
                        clickOn(topic().renameButton()).type(KeyCode.ENTER))
                .topicSelectedItem(renamedTopic)
                .topicItems(renamedTopic)
                .assertApp();

        assertThat(storage.readInteraction(I1.INTERACTION.id()).orElseThrow().topicId()).isEqualTo(I1.TOPIC_ID);
    }
}

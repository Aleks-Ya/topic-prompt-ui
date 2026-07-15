package gptui.ui.answer;

import gptui.BaseGptUiTest;
import gptui.core.storagefilesystem.Answer;
import gptui.core.storagefilesystem.Interaction;
import gptui.core.storagefilesystem.InteractionId;
import gptui.core.storagefilesystem.InteractionType;
import gptui.core.storagefilesystem.Topic;
import gptui.core.storagefilesystem.TopicId;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static gptui.core.storagefilesystem.AnswerState.SUCCESS;
import static gptui.core.storagefilesystem.AnswerType.GRAMMAR;
import static org.assertj.core.api.Assertions.assertThat;

class AnswerDetailsDialogTest extends BaseGptUiTest {
    private static final TopicId TOPIC_ID = new TopicId(100L);
    private static final Topic TOPIC = new Topic(TOPIC_ID, "Details Topic");
    private static final Answer GRAMMAR_ANSWER = new Answer(GRAMMAR, "Grammar prompt", "Grammar MD", "Grammar HTML",
            SUCCESS, "resp-1", "grammar-model", "low", "completed", 10, 20, 30);
    private static final Interaction INTERACTION = new Interaction(new InteractionId(100L), InteractionType.QUESTION,
            TOPIC_ID, "Details question", Map.of(GRAMMAR, GRAMMAR_ANSWER), null);

    @Override
    public void init() {
        storage.saveTopic(TOPIC);
        storage.saveInteraction(INTERACTION);
    }

    @Test
    void showsAnswerDetailsForAnsweredPane() {
        clickOn(grammarAnswer().button());
        var dialog = answerDetailsDialog();
        assertThat(dialog.answerTypeField().getText()).isEqualTo("GRAMMAR");
        assertThat(dialog.modelIdField().getText()).isEqualTo("grammar-model");
        assertThat(dialog.effortLevelField().getText()).isEqualTo("low");
        assertThat(dialog.finishReasonField().getText()).isEqualTo("completed");
        assertThat(dialog.inputTokensField().getText()).isEqualTo("10");
        assertThat(dialog.outputTokensField().getText()).isEqualTo("20");
        assertThat(dialog.totalTokensField().getText()).isEqualTo("30");
        assertThat(dialog.promptArea().getText()).isEqualTo("Grammar prompt");
        type(KeyCode.ESCAPE);
    }

    @Test
    void showsBlankFieldsForUnansweredPane() {
        clickOn(openAiAnswer().button());
        var dialog = answerDetailsDialog();
        assertThat(dialog.answerTypeField().getText()).isEqualTo("OPEN_AI");
        assertThat(dialog.modelIdField().getText()).isEmpty();
        assertThat(dialog.effortLevelField().getText()).isEmpty();
        assertThat(dialog.finishReasonField().getText()).isEmpty();
        assertThat(dialog.inputTokensField().getText()).isEmpty();
        assertThat(dialog.outputTokensField().getText()).isEmpty();
        assertThat(dialog.totalTokensField().getText()).isEmpty();
        assertThat(dialog.promptArea().getText()).isEmpty();
        type(KeyCode.ESCAPE);
    }
}

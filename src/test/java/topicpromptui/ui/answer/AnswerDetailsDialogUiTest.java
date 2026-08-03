package topicpromptui.ui.answer;

import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.core.domain.Answer;
import topicpromptui.core.domain.Interaction;
import topicpromptui.core.domain.InteractionId;
import topicpromptui.core.domain.InteractionType;
import topicpromptui.core.domain.Topic;
import topicpromptui.core.domain.TopicId;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static topicpromptui.core.domain.AnswerState.SUCCESS;
import static topicpromptui.core.domain.AnswerType.GRAMMAR;
import static org.assertj.core.api.Assertions.assertThat;

class AnswerDetailsDialogUiTest extends BaseTopicPromptUiTest {
    private static final TopicId TOPIC_ID = new TopicId(100L);
    private static final Topic TOPIC = new Topic(TOPIC_ID, "Details Topic");
    // Synthetic rendering fixture: the toolCalls lines exercise the "Tools used" area regardless of pane.
    private static final Answer GRAMMAR_ANSWER = new Answer(GRAMMAR, "Grammar prompt", "Grammar MD", "Grammar HTML",
            SUCCESS, "resp-1", "grammar-model", "low", "completed", 10, 20, 30)
            .withToolCalls(List.of("context7 · resolve-library-id {\"libraryName\":\"react\"}",
                    "context7 · get-library-docs {\"context7CompatibleLibraryID\":\"/facebook/react\"}"))
            .withSystemPrompt("Grammar system prompt");
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
        assertThat(dialog.interactionIdField().getText()).isEqualTo("100");
        assertThat(dialog.answerTypeField().getText()).isEqualTo("GRAMMAR");
        assertThat(dialog.modelIdField().getText()).isEqualTo("grammar-model");
        assertThat(dialog.effortLevelField().getText()).isEqualTo("low");
        assertThat(dialog.finishReasonField().getText()).isEqualTo("completed");
        assertThat(dialog.inputTokensField().getText()).isEqualTo("10");
        assertThat(dialog.outputTokensField().getText()).isEqualTo("20");
        assertThat(dialog.totalTokensField().getText()).isEqualTo("30");
        assertThat(dialog.toolsUsedArea().getText()).isEqualTo(
                "context7 · resolve-library-id {\"libraryName\":\"react\"}\n"
                        + "context7 · get-library-docs {\"context7CompatibleLibraryID\":\"/facebook/react\"}");
        assertThat(dialog.promptArea().getText()).isEqualTo("Grammar prompt");
        assertThat(dialog.systemPromptArea().getText()).isEqualTo("Grammar system prompt");

        clickOn(dialog.openInteractionFileButton());
        assertThat(fileModel.getOpenedFiles()).containsExactly(storage.getInteractionFilePath(new InteractionId(100L)));

        type(KeyCode.ESCAPE);
    }

    @Test
    void showsBlankFieldsForUnansweredPane() {
        clickOn(openAiAnswer().button());
        var dialog = answerDetailsDialog();
        assertThat(dialog.interactionIdField().getText()).isEqualTo("100");
        assertThat(dialog.answerTypeField().getText()).isEqualTo("OPEN_AI");
        assertThat(dialog.modelIdField().getText()).isEmpty();
        assertThat(dialog.effortLevelField().getText()).isEmpty();
        assertThat(dialog.finishReasonField().getText()).isEmpty();
        assertThat(dialog.inputTokensField().getText()).isEmpty();
        assertThat(dialog.outputTokensField().getText()).isEmpty();
        assertThat(dialog.totalTokensField().getText()).isEmpty();
        assertThat(dialog.toolsUsedArea().getText()).isEmpty();
        assertThat(dialog.promptArea().getText()).isEmpty();
        assertThat(dialog.systemPromptArea().getText()).isEmpty();
        type(KeyCode.ESCAPE);
    }
}

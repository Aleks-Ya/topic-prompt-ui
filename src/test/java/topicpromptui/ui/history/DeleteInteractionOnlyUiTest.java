package topicpromptui.ui.history;

import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.ui.TestingData.I0;
import topicpromptui.ui.TestingData.I1;
import org.junit.jupiter.api.Test;

import static topicpromptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EMPTY;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.WHITE;

class DeleteInteractionOnlyUiTest extends BaseTopicPromptUiTest {
    @Override
    public void init() {
        storage.saveTopic(I1.TOPIC);
        storage.saveInteraction(I1.INTERACTION);
    }

    @Test
    void currentInteractionIsTheOnly() {
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
                .ai1A().text(I1.AI_1_HTML)
                .ai2A().text(I1.AI_2_HTML)
                .ai3A().text(I1.AI_3_HTML)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)
                .assertApp();

        clickOn(history().deleteButton());

        assertion()
                .focus(topic().comboBox())
                .historySize(0, 0)
                .historyDeleteButtonDisabled(true)
                .historySelectedItem(I0.HISTORY_SELECTED_ITEM)
                .historyItems()
                .topicSize(1)
                .topicSelectedItem(I0.TOPIC_SELECTED_ITEM)
                .topicItems(I1.TOPIC)
                .topicFilterHistorySelected(false)
                .topicRenameButtonDisabled(true)
                .questionText(I1.QUESTION)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .modelEditedQuestion(I1.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I0.GRAMMAR_HTML)
                .ai1A().text(I0.AI_1_HTML)
                .ai2A().text(I0.AI_2_HTML)
                .ai3A().text(I0.AI_3_HTML)
                .answerCircleColors(WHITE, WHITE, WHITE, WHITE)
                .assertApp();
    }
}
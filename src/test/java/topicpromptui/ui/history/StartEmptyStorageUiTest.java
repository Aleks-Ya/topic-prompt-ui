package topicpromptui.ui.history;

import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.ui.TestingData.I0;
import org.junit.jupiter.api.Test;

import static topicpromptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EMPTY;
import static javafx.scene.paint.Color.WHITE;

class StartEmptyStorageUiTest extends BaseTopicPromptUiTest {
    @Test
    void startWithEmptyStorage() {
        assertion()
                .focus(history().comboBox())
                .historySize(0, 0)
                .historyDeleteButtonDisabled(true)
                .historySelectedItem(I0.HISTORY_SELECTED_ITEM)
                .historyItems()
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
                .ai1A().text(I0.AI_1_HTML)
                .ai2A().text(I0.AI_2_HTML)
                .ai3A().text(I0.AI_3_HTML)
                .answerCircleColors(WHITE, WHITE, WHITE, WHITE)
                .assertApp();
    }
}
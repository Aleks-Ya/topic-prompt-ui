package topicpromptui.ui.history;

import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.ui.TestingData.I0;
import org.junit.jupiter.api.Test;

import static topicpromptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EMPTY;
import static javafx.scene.paint.Color.WHITE;

class StartEmptyStorageTest extends BaseTopicPromptUiTest {
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
                .openAiA().text(I0.OPEN_AI_HTML)
                .claudeA().text(I0.CLAUDE_HTML)
                .gcpA().text(I0.GCP_HTML)
                .answerCircleColors(WHITE, WHITE, WHITE, WHITE)
                .assertApp();
    }
}
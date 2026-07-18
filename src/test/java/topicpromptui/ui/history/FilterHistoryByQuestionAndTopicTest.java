package topicpromptui.ui.history;

import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.ui.TestingData.I1;
import topicpromptui.ui.TestingData.I2;
import topicpromptui.ui.TestingData.I3;
import org.junit.jupiter.api.Test;

import static topicpromptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EMPTY;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.RED;

class FilterHistoryByQuestionAndTopicTest extends BaseTopicPromptUiTest {
    @Override
    public void init() {
        storage.saveTopic(I1.TOPIC);
        storage.saveTopic(I2.TOPIC);
        storage.saveTopic(I3.TOPIC);
        storage.saveInteraction(I1.INTERACTION);
        storage.saveInteraction(I2.INTERACTION);
        storage.saveInteraction(I3.INTERACTION);
    }

    @Test
    void questionTextFilterAndTopicFilterCombineWithAnd() {
        assertion()
                .focus(history().comboBox())
                .historySize(3, 3)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(I3.INTERACTION)
                .historyItems(I3.INTERACTION, I2.INTERACTION, I1.INTERACTION)
                .historyFilterText("")
                .topicSize(3)
                .topicSelectedItem(I3.TOPIC)
                .topicItems(I3.TOPIC, I2.TOPIC, I1.TOPIC)
                .topicFilterHistorySelected(false)
                .questionText(I3.QUESTION)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .modelEditedQuestion(I3.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I3.GRAMMAR_HTML)
                .openAiA().text(I3.OPEN_AI_HTML)
                .claudeA().text(I3.CLAUDE_HTML)
                .gcpA().text(I3.GCP_HTML)
                .answerCircleColors(GREEN, GREEN, RED, GREEN)

                .work("Filter by topic", () -> clickOn(topic().filterHistoryCheckBox()))
                .focus(topic().filterHistoryCheckBox())
                .historySize(1, 3)
                .historyItems(I3.INTERACTION)
                .topicFilterHistorySelected(true)

                // Topic filter keeps only I3 ("Topic 3"), question filter keeps only I1 ("Question 1"):
                // AND semantics leave nothing, while the current interaction (I3) stays displayed.
                .work("Add question filter", () -> clickOn(history().filterTextField()).write("1"))
                .focus(history().filterTextField())
                .historySize(0, 3)
                .historyItems()
                .historySelectedItem(null)
                .modelCurrentInteraction(I3.INTERACTION)
                .historyFilterText("1")

                // Question filter alone applies again; the current interaction is unchanged
                // (unticking does not re-point it), so the selection stays cleared.
                .work("Remove topic filter", () -> clickOn(topic().filterHistoryCheckBox()))
                .focus(topic().filterHistoryCheckBox())
                .historySize(1, 3)
                .historyItems(I1.INTERACTION)
                .topicFilterHistorySelected(false)
                .assertApp();
    }
}

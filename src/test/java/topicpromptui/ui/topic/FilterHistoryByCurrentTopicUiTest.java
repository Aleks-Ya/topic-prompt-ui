package topicpromptui.ui.topic;

import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.ui.TestingData.I1;
import topicpromptui.ui.TestingData.I2;
import topicpromptui.ui.TestingData.I3;
import org.junit.jupiter.api.Test;

import static topicpromptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EMPTY;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.RED;

class FilterHistoryByCurrentTopicUiTest extends BaseTopicPromptUiTest {
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
    void filterHistory() {
        assertion()
                .focus(history().comboBox())
                .historySize(3, 3)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(I3.INTERACTION)
                .historyItems(I3.INTERACTION, I2.INTERACTION, I1.INTERACTION)
                .topicSize(3)
                .topicSelectedItem(I3.TOPIC)
                .topicItems(I3.TOPIC, I2.TOPIC, I1.TOPIC)
                .topicFilterHistorySelected(false)
                .questionText(I3.QUESTION)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .modelEditedQuestion(I3.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I3.GRAMMAR_HTML)
                .ai1A().text(I3.AI_1_HTML)
                .ai2A().text(I3.AI_2_HTML)
                .ai3A().text(I3.AI_3_HTML)
                .answerCircleColors(GREEN, GREEN, RED, GREEN)

                .work("Filter", () -> clickOn(topic().filterHistoryCheckBox()))
                .focus(topic().filterHistoryCheckBox())
                .historySize(1, 3)
                .historyItems(I3.INTERACTION)
                .topicFilterHistorySelected(true)

                .work("Remove Filter", () -> clickOn(topic().filterHistoryCheckBox()))
                .historySize(3, 3)
                .historyItems(I3.INTERACTION, I2.INTERACTION, I1.INTERACTION)
                .topicFilterHistorySelected(false)
                .assertApp();
    }
}
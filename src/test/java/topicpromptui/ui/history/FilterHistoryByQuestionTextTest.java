package topicpromptui.ui.history;

import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.ui.TestingData.I1;
import topicpromptui.ui.TestingData.I2;
import topicpromptui.ui.TestingData.I3;
import org.junit.jupiter.api.Test;

import static topicpromptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EMPTY;
import static java.lang.String.format;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.RED;

class FilterHistoryByQuestionTextTest extends BaseTopicPromptUiTest {
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
    void filterHistoryByQuestionText() {
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

                // Lowercase filter matches "Question 1" only: proves case-insensitivity.
                // The current interaction (I3) is filtered out: the combo selection clears,
                // but the current interaction and its answer panes stay untouched.
                .work("Type filter", () -> clickOn(history().filterTextField()).write("question 1"))
                .focus(history().filterTextField())
                .historySize(1, 3)
                .historyItems(I1.INTERACTION)
                .historySelectedItem(null)
                .modelCurrentInteraction(I3.INTERACTION)
                .historyFilterText("question 1")

                .work("Select filtered item", () ->
                        clickOn(history().comboBox()).clickOn(format("[Q] %s: %s", I1.TOPIC.title(), I1.QUESTION)))
                .focus(history().comboBox())
                .historySelectedItem(I1.INTERACTION)
                .modelCurrentInteraction(I1.INTERACTION)
                .topicSelectedItem(I1.TOPIC)
                .questionText(I1.QUESTION)
                .modelEditedQuestion(I1.QUESTION)
                .grammarA().text(I1.GRAMMAR_HTML)
                .openAiA().text(I1.OPEN_AI_HTML)
                .claudeA().text(I1.CLAUDE_HTML)
                .gcpA().text(I1.GCP_HTML)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)

                .work("Clear filter", () -> clickOn(history().filterTextField()).eraseText("question 1".length()))
                .focus(history().filterTextField())
                .historySize(3, 3)
                .historyItems(I3.INTERACTION, I2.INTERACTION, I1.INTERACTION)
                .historyFilterText("")
                .assertApp();
    }
}

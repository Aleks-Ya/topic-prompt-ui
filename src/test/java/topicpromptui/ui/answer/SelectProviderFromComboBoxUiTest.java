package topicpromptui.ui.answer;

import org.junit.jupiter.api.Test;
import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.ui.TestingData.I1;

import static java.time.Duration.ZERO;
import static javafx.scene.paint.Color.GREEN;
import static org.assertj.core.api.Assertions.assertThat;
import static topicpromptui.core.domain.AiProvider.CLAUDE;
import static topicpromptui.core.domain.AiProvider.GCP;
import static topicpromptui.core.domain.AiProvider.OPEN_AI;
import static topicpromptui.core.domain.AiProvider.OPEN_AI_GRAMMAR;
import static topicpromptui.core.domain.AiProvider.XAI;
import static topicpromptui.core.domain.AnswerType.AI_2;
import static topicpromptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EDITED;

/**
 * Picking a provider in a pane's ComboBox must route that pane's requests to it and persist the choice.
 * Claude is the target because it backs no pane by default and its mock is a distinct instance, unlike the
 * OpenAI mock that serves both OpenAI slots.
 */
class SelectProviderFromComboBoxUiTest extends BaseTopicPromptUiTest {
    @Override
    public void init() {
        storage.saveTopic(I1.TOPIC);
        storage.saveInteraction(I1.INTERACTION);
    }

    @Test
    void providerChosenInTheComboBoxAnswersInItsPane() {
        assertThat(providerSelection.getAll()).containsEntry(AI_2, XAI);

        clickOn(ai2Answer().providerComboBox()).clickOn("Claude");

        assertThat(providerSelection.getAll()).containsEntry(AI_2, CLAUDE);
        assertThat(configModel.getProperty("ai.provider.ai_2")).isEqualTo("CLAUDE");

        gptApi.clear()
                .putGrammarResponse("Question 2", ZERO)
                .putFactResponse("Fact from OpenAI", ZERO);
        xaiApi.clear();
        claudeApi.clear().putFactResponse("Fact from Claude", ZERO);
        gcpApi.clear().putFactResponse("Fact from Gemini", ZERO);

        clickOn(question().textArea());
        overWrite("Question 2");
        clickOn(question().factButton());
        claudeApi.waitUntilSent(1);

        assertion()
                .focus(question().factButton())
                .historySize(2, 2)
                .historySelectedItem(storage.readAllInteractions().getFirst())
                .historyItems(storage.readAllInteractions())
                .topicSize(1)
                .topicSelectedItem(I1.TOPIC)
                .topicItems(I1.TOPIC)
                .topicFilterHistorySelected(false)
                .questionText("Question 2")
                .questionStyle(QUESTION_STYLE_EDITED)
                .modelEditedQuestion("Question 2")
                .modelIsEnteringNewQuestion(false)
                .answerProviders(OPEN_AI_GRAMMAR, OPEN_AI, CLAUDE, GCP)
                .grammarA().text("<p>Question 2</p>\n")
                .ai1A().text("<p>Fact from OpenAI</p>\n")
                .ai2A().text("<p>Fact from Claude</p>\n")
                .ai3A().text("<p>Fact from Gemini</p>\n")
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)
                .assertApp();

        assertThat(claudeApi.getSendHistory()).hasSize(1);
        assertThat(xaiApi.getSendHistory()).isEmpty();
    }
}

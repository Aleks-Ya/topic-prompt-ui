package topicpromptui.ui.answer;

import org.junit.jupiter.api.Test;
import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.ui.TestingData.I1;

import static java.time.Duration.ZERO;
import static javafx.scene.paint.Color.GREEN;
import static org.assertj.core.api.Assertions.assertThat;
import static topicpromptui.core.domain.AiProvider.CLAUDE;
import static topicpromptui.core.domain.AiProvider.XAI;
import static topicpromptui.core.domain.AnswerType.AI_2;
import static topicpromptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EDITED;

/**
 * Choosing another provider for a pane must route that pane's requests to it and relabel its button —
 * without touching the other panes. Claude is the target because it backs no pane by default and its mock
 * is a distinct instance, unlike the OpenAI mock that serves both OpenAI slots.
 */
class SelectAnswerProviderUiTest extends BaseTopicPromptUiTest {
    @Override
    public void init() {
        storage.saveTopic(I1.TOPIC);
        storage.saveInteraction(I1.INTERACTION);
    }

    @Test
    void selectedProviderAnswersInItsPane() {
        assertThat(providerSelection.getAll()).containsEntry(AI_2, XAI);

        selectProvider(AI_2, CLAUDE);

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
                .answerCaptions("OpenAI mini:", "OpenAI:", "Claude:", "Gemini:")
                .grammarA().text("<p>Question 2</p>\n")
                .ai1A().text("<p>Fact from OpenAI</p>\n")
                .ai2A().text("<p>Fact from Claude</p>\n")
                .ai3A().text("<p>Fact from Gemini</p>\n")
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)
                .assertApp();

        assertThat(claudeApi.getSendHistory()).hasSize(1);
        assertThat(xaiApi.getSendHistory()).isEmpty();
        assertThat(configModel.getProperty("ai.provider.ai_2")).isEqualTo("CLAUDE");
    }
}

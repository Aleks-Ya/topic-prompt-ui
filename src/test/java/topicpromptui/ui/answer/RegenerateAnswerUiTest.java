package topicpromptui.ui.answer;

import org.junit.jupiter.api.Test;
import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.ui.TestingData.I1;
import topicpromptui.ui.TestingData.I2;
import topicpromptui.ui.TestingData.I3;

import static java.time.Duration.ZERO;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.RED;
import static topicpromptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EMPTY;

class RegenerateAnswerUiTest extends BaseTopicPromptUiTest {

    @Override
    public void init() {
        storage.saveTopic(I1.TOPIC);
        storage.saveTopic(I2.TOPIC);
        storage.saveInteraction(I1.INTERACTION);
        storage.saveInteraction(I2.INTERACTION);
    }

    @Test
    void currentInteractionIsTheOnly() {
        assertion()
                .focus(history().comboBox())
                .historySize(2, 2)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(I2.INTERACTION)
                .historyItems(I2.INTERACTION, I1.INTERACTION)
                .topicSize(2)
                .topicSelectedItem(I2.TOPIC)
                .topicItems(I2.TOPIC, I1.TOPIC)
                .topicFilterHistorySelected(false)
                .questionText(I2.QUESTION)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .modelEditedQuestion(I2.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I2.GRAMMAR_HTML)
                .ai1A().text(I2.AI_1_HTML)
                .ai2A().text(I2.AI_2_HTML)
                .ai3A().text(I2.AI_3_HTML)
                .answerCircleColors(GREEN, GREEN, RED, GREEN)

                .work("Wait for Regenerate Grammar Answer Response", () -> {
                    gptApi.clear().putGrammarResponse(I2.GRAMMAR_ANSWER, ZERO);
                    clickOn(grammarAnswer().regenerateButton());
                    gptApi.waitUntilSent(1);
                })
                .focus(grammarAnswer().regenerateButton())
                .historyItems(storage.readInteraction(I2.INTERACTION.id()).orElseThrow(), I1.INTERACTION)
                .grammarA().text(I2.EXP_GRAMMAR_ANSWER_BODY)

                .work("regenerateOpenAiAnswer", () -> {
                    gptApi.clear().putOpenAiResponse(I3.AI_1_HTML, ZERO);
                    clickOn(ai1Answer().regenerateButton());
                    gptApi.waitUntilSent(1);
                })
                .focus(ai1Answer().regenerateButton())
                .historyItems(storage.readInteraction(I2.INTERACTION.id()).orElseThrow(), I1.INTERACTION)
                .ai1A().text(I3.EXP_AI_1_HTML_BODY)

                .work("Regenerate Grok Answer", () -> {
                    xaiApi.clear().putGrokResponse(I3.AI_2_HTML, ZERO);
                    clickOn(ai2Answer().regenerateButton());
                    xaiApi.waitUntilSent(1);
                })
                .focus(ai2Answer().regenerateButton())
                .historyItems(storage.readInteraction(I2.INTERACTION.id()).orElseThrow(), I1.INTERACTION)
                .ai2A().text(I3.EXP_AI_2_HTML_BODY)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)

                .work("Regenerate GCP Answer", () -> {
                    gcpApi.clear().putGcpResponse(I3.AI_3_HTML, ZERO);
                    clickOn(ai3Answer().regenerateButton());
                    gcpApi.waitUntilSent(1);
                })
                .focus(ai3Answer().regenerateButton())
                .historyItems(storage.readInteraction(I2.INTERACTION.id()).orElseThrow(), I1.INTERACTION)
                .ai3A().text(I3.EXP_AI_3_HTML_BODY)

                .work("Choose Interaction 1 from history", () -> {
                    clickOn(history().comboBox());
                    clickOn("[Q] " + I1.TOPIC.title() + ": " + I1.QUESTION);
                })
                .focus(history().comboBox())
                .historySelectedItem(storage.readInteraction(I1.INTERACTION.id()).orElseThrow())
                .topicSelectedItem(I1.TOPIC)
                .questionText(I1.QUESTION)
                .modelEditedQuestion(I1.QUESTION)
                .grammarA().text(I1.GRAMMAR_HTML)
                .ai1A().text(I1.AI_1_HTML)
                .ai2A().text(I1.AI_2_HTML)
                .ai3A().text(I1.AI_3_HTML)

                .work("Regenerate GCP Answer", () -> {
                    gcpApi.clear().putGcpResponse(I3.AI_3_HTML, ZERO);
                    clickOn(ai3Answer().regenerateButton());
                    gcpApi.waitUntilSent(1);
                })
                .focus(ai3Answer().regenerateButton())
                .historyItems(storage.readInteraction(I2.INTERACTION.id()).orElseThrow(), storage.readInteraction(I1.INTERACTION.id()).orElseThrow())
                .ai3A().text(I3.EXP_AI_3_HTML_BODY)

                .assertApp();
    }

}
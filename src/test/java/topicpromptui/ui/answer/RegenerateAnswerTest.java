package topicpromptui.ui.answer;

import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.ui.TestingData.I1;
import topicpromptui.ui.TestingData.I2;
import topicpromptui.ui.TestingData.I3;
import org.junit.jupiter.api.Test;

import static topicpromptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EMPTY;
import static java.time.Duration.ZERO;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.RED;

class RegenerateAnswerTest extends BaseTopicPromptUiTest {

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
                .openAiA().text(I2.OPEN_AI_HTML)
                .claudeA().text(I2.CLAUDE_HTML)
                .gcpA().text(I2.GCP_HTML)
                .answerCircleColors(GREEN, GREEN, RED, GREEN)

                .work("Wait for Regenerate Grammar Answer Response", () -> {
                    gptApi.clear().putGrammarResponse(I3.GRAMMAR_HTML, ZERO);
                    clickOn(grammarAnswer().regenerateButton());
                    gptApi.waitUntilSent(1);
                })
                .focus(grammarAnswer().regenerateButton())
                .historyItems(storage.readInteraction(I2.INTERACTION.id()).orElseThrow(), I1.INTERACTION)
                .grammarA().text(I3.EXP_GRAMMAR_HTML_BODY)

                .work("regenerateOpenAiAnswer", () -> {
                    gptApi.clear().putOpenAiResponse(I3.OPEN_AI_HTML, ZERO);
                    clickOn(openAiAnswer().regenerateButton());
                    gptApi.waitUntilSent(1);
                })
                .focus(openAiAnswer().regenerateButton())
                .historyItems(storage.readInteraction(I2.INTERACTION.id()).orElseThrow(), I1.INTERACTION)
                .openAiA().text(I3.EXP_OPEN_AI_HTML_BODY)

                .work("Regenerate Claude Answer", () -> {
                    claudeApi.clear().putClaudeResponse(I3.CLAUDE_HTML, ZERO);
                    clickOn(claudeAnswer().regenerateButton());
                    claudeApi.waitUntilSent(1);
                })
                .focus(claudeAnswer().regenerateButton())
                .historyItems(storage.readInteraction(I2.INTERACTION.id()).orElseThrow(), I1.INTERACTION)
                .claudeA().text(I3.EXP_CLAUDE_HTML_BODY)
                .answerCircleColors(GREEN, GREEN, GREEN, GREEN)

                .work("Regenerate GCP Answer", () -> {
                    gcpApi.clear().putGcpResponse(I3.GCP_HTML, ZERO);
                    clickOn(gcpAnswer().regenerateButton());
                    gcpApi.waitUntilSent(1);
                })
                .focus(gcpAnswer().regenerateButton())
                .historyItems(storage.readInteraction(I2.INTERACTION.id()).orElseThrow(), I1.INTERACTION)
                .gcpA().text(I3.EXP_GCP_HTML_BODY)

                .work("Choose Interaction 1 from history", () -> {
                    clickOn(history().comboBox());
                    clickOn(String.format("[Q] %s: %s", I1.TOPIC.title(), I1.QUESTION));
                })
                .focus(history().comboBox())
                .historySelectedItem(storage.readInteraction(I1.INTERACTION.id()).orElseThrow())
                .topicSelectedItem(I1.TOPIC)
                .questionText(I1.QUESTION)
                .modelEditedQuestion(I1.QUESTION)
                .grammarA().text(I1.GRAMMAR_HTML)
                .openAiA().text(I1.OPEN_AI_HTML)
                .claudeA().text(I1.CLAUDE_HTML)
                .gcpA().text(I1.GCP_HTML)

                .work("Regenerate GCP Answer", () -> {
                    gcpApi.clear().putGcpResponse(I3.GCP_HTML, ZERO);
                    clickOn(gcpAnswer().regenerateButton());
                    gcpApi.waitUntilSent(1);
                })
                .focus(gcpAnswer().regenerateButton())
                .historyItems(storage.readInteraction(I2.INTERACTION.id()).orElseThrow(), storage.readInteraction(I1.INTERACTION.id()).orElseThrow())
                .gcpA().text(I3.EXP_GCP_HTML_BODY)

                .assertApp();
    }

}
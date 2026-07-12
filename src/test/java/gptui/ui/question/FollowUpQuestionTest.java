package gptui.ui.question;

import gptui.BaseGptUiTest;
import gptui.ui.TestingData.I1;
import org.junit.jupiter.api.Test;

import static gptui.core.storagefilesystem.AnswerType.OPEN_AI;
import static java.time.Duration.ZERO;
import static org.assertj.core.api.Assertions.assertThat;

class FollowUpQuestionTest extends BaseGptUiTest {
    @Override
    public void init() {
        storage.saveTheme(I1.THEME);
        storage.saveInteraction(I1.INTERACTION);
    }

    @Test
    void followUpCheckboxLinksNewInteractionAndSendsHistory() {
        gptApi.clear().putGrammarResponse("Correct", ZERO).putResponse("Who created it?", "James Gosling created it.", ZERO);
        claudeApi.clear().putResponse("Who created it?", "James Gosling created it.", ZERO);
        gcpApi.clear().putResponse("Who created it?", "James Gosling created it.", ZERO);

        clickOn(question().followUpCheckBox());
        clickOn(question().textArea());
        overWrite("Who created it?");
        clickOn(question().questionButton());

        gptApi.waitUntilSent(2);
        claudeApi.waitUntilSent(1);
        gcpApi.waitUntilSent(1);

        var followUpInteractionId = stateModel.getCurrentInteractionId();
        var followUpInteraction = storage.readInteraction(followUpInteractionId).orElseThrow();
        assertThat(followUpInteraction.parentInteractionId()).isEqualTo(I1.INTERACTION.id());

        var openAiAncestorAnswer = I1.INTERACTION.getAnswer(OPEN_AI).orElseThrow();
        var turns = gptApi.getTurnsSendHistory().stream().filter(t -> t.size() > 1).findFirst().orElseThrow();
        assertThat(turns).hasSize(3);
        assertThat(turns.get(0).content()).isEqualTo(openAiAncestorAnswer.prompt());
        assertThat(turns.get(1).content()).isEqualTo(openAiAncestorAnswer.answerMd());
        assertThat(turns.get(2).content()).isEqualTo("Who created it?");

        assertThat(claudeApi.getTurnsSendHistory().getLast()).hasSize(3);
        assertThat(gcpApi.getTurnsSendHistory().getLast()).hasSize(3);
    }
}

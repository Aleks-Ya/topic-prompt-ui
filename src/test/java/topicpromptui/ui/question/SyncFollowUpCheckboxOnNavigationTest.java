package topicpromptui.ui.question;

import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.core.storagefilesystem.Interaction;
import topicpromptui.ui.TestingData.I1;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;

import static javafx.scene.input.KeyCode.ALT;
import static javafx.scene.input.KeyCode.CONTROL;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.UP;
import static java.time.Duration.ZERO;
import static org.assertj.core.api.Assertions.assertThat;

class SyncFollowUpCheckboxOnNavigationTest extends BaseTopicPromptUiTest {
    @Override
    public void init() {
        storage.saveTopic(I1.TOPIC);
        storage.saveInteraction(I1.INTERACTION);
    }

    @Test
    void followUpCheckboxTracksCurrentInteractionAsHistoryIsNavigated() {
        // Send a follow-up question, producing interaction B (parentInteractionId == I1) as current.
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

        var followUp = storage.readInteraction(stateModel.getCurrentInteractionId()).orElseThrow();
        assertThat(followUp.parentInteractionId()).isEqualTo(I1.INTERACTION.id());
        assertThat(question().followUpCheckBox().isSelected()).isTrue();

        // Navigate to the non-follow-up interaction I1 -> checkbox unchecks.
        press(CONTROL, ALT, DOWN).release(DOWN, ALT, CONTROL);
        WaitForAsyncUtils.waitForFxEvents();
        assertThat(history().comboBox().getSelectionModel().getSelectedItem().interaction())
                .extracting(Interaction::id).isEqualTo(I1.INTERACTION.id());
        assertThat(question().followUpCheckBox().isSelected()).isFalse();

        // Navigate back to the follow-up interaction B -> checkbox re-checks.
        press(CONTROL, ALT, UP).release(UP, ALT, CONTROL);
        WaitForAsyncUtils.waitForFxEvents();
        assertThat(history().comboBox().getSelectionModel().getSelectedItem().interaction())
                .extracting(Interaction::id).isEqualTo(followUp.id());
        assertThat(question().followUpCheckBox().isSelected()).isTrue();
    }
}

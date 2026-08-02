package topicpromptui.ui.question;

import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.ui.TestingData.I1;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;

import static javafx.scene.input.KeyCode.ALT;
import static javafx.scene.input.KeyCode.U;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * A follow-up inherits its parent's topic, so the Topic combobox is locked while the
 * Follow-up checkbox is selected (via mouse or the Alt-U hotkey) and re-enabled when cleared.
 */
class DisableTopicComboBoxForFollowUpTest extends BaseTopicPromptUiTest {
    @Override
    public void init() {
        storage.saveTopic(I1.TOPIC);
        storage.saveInteraction(I1.INTERACTION);
    }

    @Test
    void topicComboBoxIsDisabledWhileFollowUpIsSelected() {
        assertThat(topic().comboBox().isDisabled()).isFalse();

        // Toggle via mouse click on the checkbox.
        clickOn(question().followUpCheckBox());
        WaitForAsyncUtils.waitForFxEvents();
        assertThat(question().followUpCheckBox().isSelected()).isTrue();
        assertThat(topic().comboBox().isDisabled()).isTrue();

        clickOn(question().followUpCheckBox());
        WaitForAsyncUtils.waitForFxEvents();
        assertThat(question().followUpCheckBox().isSelected()).isFalse();
        assertThat(topic().comboBox().isDisabled()).isFalse();

        // Toggle via the Alt-U accelerator.
        press(ALT, U).release(U, ALT);
        WaitForAsyncUtils.waitForFxEvents();
        assertThat(question().followUpCheckBox().isSelected()).isTrue();
        assertThat(topic().comboBox().isDisabled()).isTrue();

        press(ALT, U).release(U, ALT);
        WaitForAsyncUtils.waitForFxEvents();
        assertThat(question().followUpCheckBox().isSelected()).isFalse();
        assertThat(topic().comboBox().isDisabled()).isFalse();
    }
}

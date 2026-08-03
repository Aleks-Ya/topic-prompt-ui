package topicpromptui.ui.model.state;

import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.core.domain.InteractionId;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;

import static javafx.scene.input.KeyCode.A;
import static javafx.scene.input.KeyCode.B;
import static javafx.scene.input.KeyCode.C;
import static org.assertj.core.api.Assertions.assertThat;

class StateModelUiTest extends BaseTopicPromptUiTest {
    @Test
    void typeQuestion() {
        assertThat(stateModel.getEditedQuestion()).isNull();
        clickOn(question().textArea()).type(A, B, C);
        WaitForAsyncUtils.waitForFxEvents();
        assertThat(stateModel.getEditedQuestion()).isEqualTo("abc");
    }

    @Test
    void getInteractionFilePath() {
        var interactionId = new InteractionId(1L);
        assertThat(stateModel.getInteractionFilePath(interactionId))
                .isEqualTo(storage.getInteractionFilePath(interactionId));
    }
}
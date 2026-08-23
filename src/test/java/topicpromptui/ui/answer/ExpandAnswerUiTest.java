package topicpromptui.ui.answer;

import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.ui.TestingData.I1;
import topicpromptui.ui.TestingData.I2;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;

import static javafx.scene.input.KeyCode.CONTROL;
import static javafx.scene.input.KeyCode.DIGIT1;
import static javafx.scene.input.KeyCode.DIGIT2;
import static javafx.scene.input.KeyCode.DIGIT3;
import static javafx.scene.input.KeyCode.DIGIT4;
import static javafx.scene.input.KeyCode.ESCAPE;
import static org.assertj.core.api.Assertions.assertThat;

class ExpandAnswerUiTest extends BaseTopicPromptUiTest {

    @Override
    public void init() {
        storage.saveTopic(I1.TOPIC);
        storage.saveTopic(I2.TOPIC);
        storage.saveInteraction(I1.INTERACTION);
        storage.saveInteraction(I2.INTERACTION);
    }

    @Test
    void expandAndCollapseByButton() {
        clickOn(ai1Answer().expandButton());
        WaitForAsyncUtils.waitForFxEvents();
        assertShown(ai1Answer().pane());
        assertHidden(historyPane(), topicPane(), questionPane(),
                grammarAnswer().pane(), ai2Answer().pane(), ai3Answer().pane());

        clickOn(ai1Answer().expandButton());
        WaitForAsyncUtils.waitForFxEvents();
        assertShown(historyPane(), topicPane(), questionPane(),
                grammarAnswer().pane(), ai1Answer().pane(), ai2Answer().pane(), ai3Answer().pane());
    }

    @Test
    void switchExpandedPaneByAnotherExpandButtonAfterCollapse() {
        clickOn(ai3Answer().expandButton());
        WaitForAsyncUtils.waitForFxEvents();
        assertShown(ai3Answer().pane());
        assertHidden(historyPane(), topicPane(), questionPane(),
                grammarAnswer().pane(), ai1Answer().pane(), ai2Answer().pane());

        clickOn(ai3Answer().expandButton());
        clickOn(ai2Answer().expandButton());
        WaitForAsyncUtils.waitForFxEvents();
        assertShown(ai2Answer().pane());
        assertHidden(historyPane(), topicPane(), questionPane(),
                grammarAnswer().pane(), ai1Answer().pane(), ai3Answer().pane());

        clickOn(ai2Answer().expandButton());
        WaitForAsyncUtils.waitForFxEvents();
        assertShown(historyPane(), topicPane(), questionPane(),
                grammarAnswer().pane(), ai1Answer().pane(), ai2Answer().pane(), ai3Answer().pane());
    }

    @Test
    void collapseByEscThenEscFocusesQuestion() {
        clickOn(ai2Answer().expandButton());
        WaitForAsyncUtils.waitForFxEvents();
        assertShown(ai2Answer().pane());
        assertHidden(historyPane(), topicPane(), questionPane(),
                grammarAnswer().pane(), ai1Answer().pane(), ai3Answer().pane());

        press(ESCAPE).release(ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
        assertShown(historyPane(), topicPane(), questionPane(),
                grammarAnswer().pane(), ai1Answer().pane(), ai2Answer().pane(), ai3Answer().pane());
        assertThat(question().textArea().isFocused()).isFalse();

        press(ESCAPE).release(ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
        assertThat(question().textArea().isFocused()).isTrue();
    }

    @Test
    void expandAndCollapseByCtrlDigit() {
        press(CONTROL, DIGIT2).release(DIGIT2, CONTROL);
        WaitForAsyncUtils.waitForFxEvents();
        assertShown(ai1Answer().pane());
        assertHidden(historyPane(), topicPane(), questionPane(),
                grammarAnswer().pane(), ai2Answer().pane(), ai3Answer().pane());

        press(CONTROL, DIGIT2).release(DIGIT2, CONTROL);
        WaitForAsyncUtils.waitForFxEvents();
        assertShown(historyPane(), topicPane(), questionPane(),
                grammarAnswer().pane(), ai1Answer().pane(), ai2Answer().pane(), ai3Answer().pane());
    }

    @Test
    void switchExpandedPaneByCtrlDigit() {
        press(CONTROL, DIGIT1).release(DIGIT1, CONTROL);
        WaitForAsyncUtils.waitForFxEvents();
        assertShown(grammarAnswer().pane());
        assertHidden(historyPane(), topicPane(), questionPane(),
                ai1Answer().pane(), ai2Answer().pane(), ai3Answer().pane());
        assertThat(grammarAnswer().pane().getMaxHeight()).isEqualTo(Double.MAX_VALUE);

        press(CONTROL, DIGIT3).release(DIGIT3, CONTROL);
        WaitForAsyncUtils.waitForFxEvents();
        assertShown(ai2Answer().pane());
        assertHidden(historyPane(), topicPane(), questionPane(),
                grammarAnswer().pane(), ai1Answer().pane(), ai3Answer().pane());
        assertThat(grammarAnswer().pane().getMaxHeight()).isEqualTo(70.0);
        assertThat(VBox.getVgrow(grammarAnswer().pane())).isEqualTo(Priority.SOMETIMES);

        press(ESCAPE).release(ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
        assertShown(historyPane(), topicPane(), questionPane(),
                grammarAnswer().pane(), ai1Answer().pane(), ai2Answer().pane(), ai3Answer().pane());
    }

    @Test
    void expandByCtrlDigitWhileWebViewFocused() {
        clickOn(ai2Answer().webView());
        press(CONTROL, DIGIT4).release(DIGIT4, CONTROL);
        WaitForAsyncUtils.waitForFxEvents();
        assertShown(ai3Answer().pane());
        assertHidden(historyPane(), topicPane(), questionPane(),
                grammarAnswer().pane(), ai1Answer().pane(), ai2Answer().pane());

        press(CONTROL, DIGIT4).release(DIGIT4, CONTROL);
        WaitForAsyncUtils.waitForFxEvents();
        assertShown(historyPane(), topicPane(), questionPane(),
                grammarAnswer().pane(), ai1Answer().pane(), ai2Answer().pane(), ai3Answer().pane());
    }

    @Test
    void expandGrammarLiftsMaxHeightAndVgrow() {
        var pane = grammarAnswer().pane();
        assertThat(pane.getMaxHeight()).isEqualTo(70.0);
        assertThat(VBox.getVgrow(pane)).isEqualTo(Priority.SOMETIMES);

        clickOn(grammarAnswer().expandButton());
        WaitForAsyncUtils.waitForFxEvents();
        assertShown(grammarAnswer().pane());
        assertHidden(historyPane(), topicPane(), questionPane(),
                ai1Answer().pane(), ai2Answer().pane(), ai3Answer().pane());
        assertThat(pane.getMaxHeight()).isEqualTo(Double.MAX_VALUE);
        assertThat(VBox.getVgrow(pane)).isEqualTo(Priority.ALWAYS);

        clickOn(grammarAnswer().expandButton());
        WaitForAsyncUtils.waitForFxEvents();
        assertThat(pane.getMaxHeight()).isEqualTo(70.0);
        assertThat(VBox.getVgrow(pane)).isEqualTo(Priority.SOMETIMES);
    }

    private Node historyPane() {
        return paneOf(history().comboBox());
    }

    private Node topicPane() {
        return paneOf(topic().comboBox());
    }

    private Node questionPane() {
        return paneOf(question().textArea());
    }

    /** The direct child of the root VBox holding the given node, i.e. the fx:include root of its UI area. */
    private Node paneOf(Node node) {
        var root = scene().getRoot();
        while (node.getParent() != root) {
            node = node.getParent();
        }
        return node;
    }

    private void assertShown(Node... panes) {
        for (var pane : panes) {
            assertThat(pane.isVisible()).as("visible: %s", pane.getId()).isTrue();
            assertThat(pane.isManaged()).as("managed: %s", pane.getId()).isTrue();
        }
    }

    private void assertHidden(Node... panes) {
        for (var pane : panes) {
            assertThat(pane.isVisible()).as("visible: %s", pane.getId()).isFalse();
            assertThat(pane.isManaged()).as("managed: %s", pane.getId()).isFalse();
        }
    }
}

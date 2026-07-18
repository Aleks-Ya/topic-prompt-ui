package topicpromptui.ui.answer;

import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.ui.TestingData.I1;
import topicpromptui.ui.TestingData.I2;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;

import static javafx.scene.input.KeyCode.ESCAPE;
import static org.assertj.core.api.Assertions.assertThat;

class ExpandAnswerTest extends BaseTopicPromptUiTest {

    @Override
    public void init() {
        storage.saveTopic(I1.TOPIC);
        storage.saveTopic(I2.TOPIC);
        storage.saveInteraction(I1.INTERACTION);
        storage.saveInteraction(I2.INTERACTION);
    }

    @Test
    void expandAndCollapseByButton() {
        clickOn(openAiAnswer().expandButton());
        WaitForAsyncUtils.waitForFxEvents();
        assertShown(openAiAnswer().pane());
        assertHidden(historyPane(), topicPane(), questionPane(),
                grammarAnswer().pane(), claudeAnswer().pane(), gcpAnswer().pane());

        clickOn(openAiAnswer().expandButton());
        WaitForAsyncUtils.waitForFxEvents();
        assertShown(historyPane(), topicPane(), questionPane(),
                grammarAnswer().pane(), openAiAnswer().pane(), claudeAnswer().pane(), gcpAnswer().pane());
    }

    @Test
    void switchExpandedPaneByAnotherExpandButtonAfterCollapse() {
        clickOn(gcpAnswer().expandButton());
        WaitForAsyncUtils.waitForFxEvents();
        assertShown(gcpAnswer().pane());
        assertHidden(historyPane(), topicPane(), questionPane(),
                grammarAnswer().pane(), openAiAnswer().pane(), claudeAnswer().pane());

        clickOn(gcpAnswer().expandButton());
        clickOn(claudeAnswer().expandButton());
        WaitForAsyncUtils.waitForFxEvents();
        assertShown(claudeAnswer().pane());
        assertHidden(historyPane(), topicPane(), questionPane(),
                grammarAnswer().pane(), openAiAnswer().pane(), gcpAnswer().pane());

        clickOn(claudeAnswer().expandButton());
        WaitForAsyncUtils.waitForFxEvents();
        assertShown(historyPane(), topicPane(), questionPane(),
                grammarAnswer().pane(), openAiAnswer().pane(), claudeAnswer().pane(), gcpAnswer().pane());
    }

    @Test
    void collapseByEscThenEscFocusesQuestion() {
        clickOn(claudeAnswer().expandButton());
        WaitForAsyncUtils.waitForFxEvents();
        assertShown(claudeAnswer().pane());
        assertHidden(historyPane(), topicPane(), questionPane(),
                grammarAnswer().pane(), openAiAnswer().pane(), gcpAnswer().pane());

        press(ESCAPE).release(ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
        assertShown(historyPane(), topicPane(), questionPane(),
                grammarAnswer().pane(), openAiAnswer().pane(), claudeAnswer().pane(), gcpAnswer().pane());
        assertThat(question().textArea().isFocused()).isFalse();

        press(ESCAPE).release(ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
        assertThat(question().textArea().isFocused()).isTrue();
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
                openAiAnswer().pane(), claudeAnswer().pane(), gcpAnswer().pane());
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

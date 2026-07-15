package topicpromptui.ui.topic;

import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.core.storagefilesystem.Topic;
import topicpromptui.core.storagefilesystem.TopicId;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;

import static javafx.scene.input.KeyCode.ENTER;
import static org.assertj.core.api.Assertions.assertThat;

class SearchTopicShortestFirstTest extends BaseTopicPromptUiTest {
    private static final Topic AWS_IAM = new Topic(new TopicId(1L), "AWS IAM");
    private static final Topic AWS_CF = new Topic(new TopicId(2L), "AWS CloudFormation");
    private static final Topic AWS = new Topic(new TopicId(3L), "AWS");
    private static final Topic AWS_EC2 = new Topic(new TopicId(4L), "AWS EC2");

    @Override
    public void init() {
        storage.saveTopic(AWS_IAM);
        storage.saveTopic(AWS_CF);
        storage.saveTopic(AWS);
        storage.saveTopic(AWS_EC2);
    }

    @Test
    void typingSortsSuggestionsShortestFirstAndEmptySearchKeepsOriginalOrder() {
        clickOn(topic().comboBoxNarrow());
        WaitForAsyncUtils.waitForFxEvents();

        assertThat(topic().popupFilteredComboBox().getItems())
                .as("popup just opened, empty search: original order untouched")
                .containsExactly(AWS_IAM, AWS_CF, AWS, AWS_EC2);

        write("aws");
        WaitForAsyncUtils.waitForFxEvents();

        assertThat(topic().popupFilteredComboBox().getItems())
                .as("typing: shortest first; equal lengths keep original relative order")
                .containsExactly(AWS, AWS_IAM, AWS_EC2, AWS_CF);

        eraseText(3);
        WaitForAsyncUtils.waitForFxEvents();

        assertThat(topic().popupFilteredComboBox().getItems())
                .as("search cleared: back to original order")
                .containsExactly(AWS_IAM, AWS_CF, AWS, AWS_EC2);

        write("aws");
        type(ENTER);
        WaitForAsyncUtils.waitForFxEvents();

        assertThat(topic().comboBox().getValue())
                .as("ENTER selects the first (shortest) suggestion")
                .isEqualTo(AWS);
    }
}

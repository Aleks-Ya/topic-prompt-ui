package gptui.ui.theme;

import gptui.BaseGptUiTest;
import gptui.core.storagefilesystem.Theme;
import gptui.core.storagefilesystem.ThemeId;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;

import static javafx.scene.input.KeyCode.ENTER;
import static org.assertj.core.api.Assertions.assertThat;

class SearchThemeShortestFirstTest extends BaseGptUiTest {
    private static final Theme AWS_IAM = new Theme(new ThemeId(1L), "AWS IAM");
    private static final Theme AWS_CF = new Theme(new ThemeId(2L), "AWS CloudFormation");
    private static final Theme AWS = new Theme(new ThemeId(3L), "AWS");
    private static final Theme AWS_EC2 = new Theme(new ThemeId(4L), "AWS EC2");

    @Override
    public void init() {
        storage.saveTheme(AWS_IAM);
        storage.saveTheme(AWS_CF);
        storage.saveTheme(AWS);
        storage.saveTheme(AWS_EC2);
    }

    @Test
    void typingSortsSuggestionsShortestFirstAndEmptySearchKeepsOriginalOrder() {
        clickOn(theme().comboBoxNarrow());
        WaitForAsyncUtils.waitForFxEvents();

        assertThat(theme().popupFilteredComboBox().getItems())
                .as("popup just opened, empty search: original order untouched")
                .containsExactly(AWS_IAM, AWS_CF, AWS, AWS_EC2);

        write("aws");
        WaitForAsyncUtils.waitForFxEvents();

        assertThat(theme().popupFilteredComboBox().getItems())
                .as("typing: shortest first; equal lengths keep original relative order")
                .containsExactly(AWS, AWS_IAM, AWS_EC2, AWS_CF);

        eraseText(3);
        WaitForAsyncUtils.waitForFxEvents();

        assertThat(theme().popupFilteredComboBox().getItems())
                .as("search cleared: back to original order")
                .containsExactly(AWS_IAM, AWS_CF, AWS, AWS_EC2);

        write("aws");
        type(ENTER);
        WaitForAsyncUtils.waitForFxEvents();

        assertThat(theme().comboBox().getValue())
                .as("ENTER selects the first (shortest) suggestion")
                .isEqualTo(AWS);
    }
}

package gptui.ui.view;

import gptui.BaseGptUiTest;
import gptui.ui.TestingData.I1;
import gptui.ui.TestingData.I2;
import gptui.ui.TestingData.I3;
import gptui.core.storagefilesystem.Interaction;
import javafx.geometry.VerticalDirection;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;

import static gptui.core.storagefilesystem.InteractionType.DEFINITION;
import static gptui.core.storagefilesystem.InteractionType.FACT;
import static gptui.core.storagefilesystem.InteractionType.GRAMMAR;
import static gptui.core.storagefilesystem.InteractionType.QUESTION;
import static gptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EMPTY;
import static javafx.scene.input.KeyCode.ALT;
import static javafx.scene.input.KeyCode.C;
import static javafx.scene.input.KeyCode.CONTROL;
import static javafx.scene.input.KeyCode.D;
import static javafx.scene.input.KeyCode.DIGIT1;
import static javafx.scene.input.KeyCode.DIGIT2;
import static javafx.scene.input.KeyCode.DIGIT3;
import static javafx.scene.input.KeyCode.DIGIT4;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.F;
import static javafx.scene.input.KeyCode.G;
import static javafx.scene.input.KeyCode.I;
import static javafx.scene.input.KeyCode.O;
import static javafx.scene.input.KeyCode.P;
import static javafx.scene.input.KeyCode.Q;
import static javafx.scene.input.KeyCode.R;
import static javafx.scene.input.KeyCode.SPACE;
import static javafx.scene.input.KeyCode.T;
import static javafx.scene.input.KeyCode.U;
import static javafx.scene.input.KeyCode.UP;
import static javafx.scene.input.KeyCode.V;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.RED;
import static org.assertj.core.api.Assertions.assertThat;

class HotKeysTest extends BaseGptUiTest {
    private static final String CLIPBOARD_CONTENT = "From clipboard";
    private static final String CLIPBOARD_CONTENT_WRAPPED = "<html><head></head><body>" + CLIPBOARD_CONTENT + "</body></html>";

    @Override
    public void init() {
        storage.saveTopic(I1.TOPIC);
        storage.saveTopic(I2.TOPIC);
        storage.saveTopic(I3.TOPIC);
        storage.saveInteraction(I1.INTERACTION);
        storage.saveInteraction(I2.INTERACTION);
        storage.saveInteraction(I3.INTERACTION);
    }

    @Test
    void hotKeys() {
        assertion()
                .focus(history().comboBox())
                .historySize(3, 3)
                .historyDeleteButtonDisabled(false)
                .historySelectedItem(I3.INTERACTION)
                .historyItems(I3.INTERACTION, I2.INTERACTION, I1.INTERACTION)
                .topicSize(3)
                .topicSelectedItem(I3.TOPIC)
                .topicItems(I3.TOPIC, I2.TOPIC, I1.TOPIC)
                .topicFilterHistorySelected(false)
                .questionText(I3.QUESTION)
                .questionStyle(QUESTION_STYLE_EMPTY)
                .modelEditedQuestion(I3.QUESTION)
                .modelIsEnteringNewQuestion(false)
                .grammarA().text(I3.GRAMMAR_HTML)
                .openAiA().text(I3.OPEN_AI_HTML)
                .claudeA().text(I3.CLAUDE_HTML)
                .gcpA().text(I3.GCP_HTML)
                .answerCircleColors(GREEN, GREEN, RED, GREEN)

                .work("Copy Grammar Answer By Alt-1", () -> press(ALT, DIGIT1).release(DIGIT1, ALT))
                .focus(grammarAnswer().copyButton())
                .clipboard(I3.GRAMMAR_HTML)

                .work("Copy OpenAI Answer By Alt-2", () -> press(ALT, DIGIT2).release(DIGIT2, ALT))
                .focus(openAiAnswer().copyButton())
                .clipboard(I3.OPEN_AI_HTML)

                .work("Copy Claude Answer By Alt-3", () -> press(ALT, DIGIT3).release(DIGIT3, ALT))
                .focus(claudeAnswer().copyButton())
                .clipboard(I3.CLAUDE_HTML)

                .work("Copy GCP Answer By Alt-4", () -> press(ALT, DIGIT4).release(DIGIT4, ALT))
                .focus(gcpAnswer().copyButton())
                .clipboard(I3.GCP_HTML)

                .work("Paste Question From Clipboard By Ctrl-Alt-V", () -> {
                    executeSyncInFxThread(() -> clipboardModel.putHtmlToClipboard(CLIPBOARD_CONTENT_WRAPPED));
                    press(CONTROL, ALT, V).release(V, ALT, CONTROL);
                })
                .focus(gcpAnswer().copyButton())
                .questionText(CLIPBOARD_CONTENT_WRAPPED)
                .clipboard(CLIPBOARD_CONTENT)
                .modelIsEnteringNewQuestion(true)
                .modelEditedQuestion(CLIPBOARD_CONTENT_WRAPPED)

                .assertApp();

    }

    @Test
    void sendQuestionByAltQ() {
        press(ALT, Q).release(Q, ALT);
        assertThat(stateModel.getFullHistory()).hasSize(4)
                .element(0).extracting(Interaction::type).isEqualTo(QUESTION);
    }

    @Test
    void sendDefinitionByAltD() {
        press(ALT, D).release(D, ALT);
        assertThat(stateModel.getFullHistory()).hasSize(4)
                .element(0).extracting(Interaction::type).isEqualTo(DEFINITION);
    }

    @Test
    void sendGrammarByAltG() {
        press(ALT, G).release(G, ALT);
        assertThat(stateModel.getFullHistory()).hasSize(4)
                .element(0).extracting(Interaction::type).isEqualTo(GRAMMAR);
    }

    @Test
    void sendFactByAltF() {
        press(ALT, F).release(F, ALT);
        assertThat(stateModel.getFullHistory()).hasSize(4)
                .element(0).extracting(Interaction::type).isEqualTo(FACT);
    }

    @Test
    void resendByAltR() {
        gptApi.clear();
        gcpApi.clear();
        claudeApi.clear();
        assertThat(gptApi.getSendHistory()).isEmpty();
        assertThat(gcpApi.getSendHistory()).isEmpty();
        assertThat(claudeApi.getSendHistory()).isEmpty();
        press(ALT, R).release(R, ALT);
        assertThat(gptApi.getSendHistory()).hasSize(2);
        assertThat(gcpApi.getSendHistory()).hasSize(1);
        assertThat(claudeApi.getSendHistory()).hasSize(1);
    }

    @Test
    void toggleFollowUpByAltU() {
        assertThat(question().followUpCheckBox().isSelected()).isFalse();
        press(ALT, U).release(U, ALT);
        WaitForAsyncUtils.waitForFxEvents();
        assertThat(question().followUpCheckBox().isSelected()).isTrue();
        press(ALT, U).release(U, ALT);
        WaitForAsyncUtils.waitForFxEvents();
        assertThat(question().followUpCheckBox().isSelected()).isFalse();
    }

    @Test
    void focusOnQuestionByEsc() {
        assertThat(question().textArea().isFocused()).isFalse();
        press(ESCAPE).release(ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
        assertThat(question().textArea().isFocused()).isTrue();
    }

    @Test
    void sendQuestionByCtrlEnter() {
        press(CONTROL, ENTER).release(ENTER, CONTROL);
        assertThat(stateModel.getFullHistory()).hasSize(4)
                .element(0).extracting(Interaction::type).isEqualTo(QUESTION);
    }

    @Test
    void selectNextInteractionByCtrlAltUp() {
        clickOn(history().comboBox()).clickOn(String.format("[Q] %s: %s", I1.TOPIC.title(), I1.QUESTION));
        assertThat(history().comboBox().getSelectionModel().getSelectedItem().interaction()).isEqualTo(I1.INTERACTION);

        press(CONTROL, ALT, UP).release(UP, ALT, CONTROL);
        assertThat(history().comboBox().getSelectionModel().getSelectedItem().interaction()).isEqualTo(I2.INTERACTION);

        press(CONTROL, ALT, UP).release(UP, ALT, CONTROL);
        assertThat(history().comboBox().getSelectionModel().getSelectedItem().interaction()).isEqualTo(I3.INTERACTION);

        press(CONTROL, ALT, UP).release(UP, ALT, CONTROL);
        assertThat(history().comboBox().getSelectionModel().getSelectedItem().interaction()).isEqualTo(I3.INTERACTION);
    }

    @Test
    void selectPreviousInteractionByCtrlAltDown() {
        clickOn(history().comboBox()).clickOn(String.format("[Q] %s: %s", I3.TOPIC.title(), I3.QUESTION));
        assertThat(history().comboBox().getSelectionModel().getSelectedItem().interaction()).isEqualTo(I3.INTERACTION);

        press(CONTROL, ALT, DOWN).release(DOWN, ALT, CONTROL);
        assertThat(history().comboBox().getSelectionModel().getSelectedItem().interaction()).isEqualTo(I2.INTERACTION);

        press(CONTROL, ALT, DOWN).release(DOWN, ALT, CONTROL);
        assertThat(history().comboBox().getSelectionModel().getSelectedItem().interaction()).isEqualTo(I1.INTERACTION);

        press(CONTROL, ALT, DOWN).release(DOWN, ALT, CONTROL);
        assertThat(history().comboBox().getSelectionModel().getSelectedItem().interaction()).isEqualTo(I1.INTERACTION);
    }

    @Test
    void selectPreviousInteractionByCtrlAltDown_FocusOnWebView() {
        clickOn(history().comboBox()).clickOn(String.format("[Q] %s: %s", I3.TOPIC.title(), I3.QUESTION));
        clickOn(claudeAnswer().webView());
        assertThat(history().comboBox().getSelectionModel().getSelectedItem().interaction()).isEqualTo(I3.INTERACTION);

        press(CONTROL, ALT, DOWN).release(CONTROL, ALT, DOWN);
        assertThat(history().comboBox().getSelectionModel().getSelectedItem().interaction()).isEqualTo(I2.INTERACTION);
    }

    @Test
    void selectPreviousInteractionByCtrlAltUp_FocusOnWebView() {
        clickOn(history().comboBox()).clickOn(String.format("[Q] %s: %s", I2.TOPIC.title(), I2.QUESTION));
        clickOn(claudeAnswer().webView());
        scroll(10, VerticalDirection.DOWN);
        assertThat(history().comboBox().getSelectionModel().getSelectedItem().interaction()).isEqualTo(I2.INTERACTION);

        press(CONTROL, ALT, UP).release(UP, ALT, CONTROL);
        assertThat(history().comboBox().getSelectionModel().getSelectedItem().interaction()).isEqualTo(I1.INTERACTION);
    }

    @Test
    void altTFocusOnTopicComboBox() {
        assertThat(topic().comboBox().isFocused()).isFalse();
        assertThat(topic().comboBox().getSelectionModel().getSelectedItem()).isEqualTo(I3.TOPIC);
        press(ALT, T).release(ALT, T);
        assertThat(topic().comboBox().isFocused()).isTrue();
        type(T, O, P, I, C, SPACE, DIGIT2, ENTER);
        assertThat(topic().comboBox().getSelectionModel().getSelectedItem()).isEqualTo(I2.TOPIC);
    }
}
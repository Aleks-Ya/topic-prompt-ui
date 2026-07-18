package topicpromptui;

import com.google.inject.util.Modules;
import topicpromptui.core.ai.claude.MockClaudeApi;
import topicpromptui.core.ai.gcp.MockGcpApi;
import topicpromptui.core.ai.openai.MockOpenAiApi;
import topicpromptui.ui.model.clipboard.ClipboardModel;
import topicpromptui.ui.model.search.HistorySearchModel;
import topicpromptui.ui.model.state.StateModel;
import topicpromptui.ui.model.storage.StorageModel;
import topicpromptui.core.storagefilesystem.Topic;
import topicpromptui.ui.view.TopicPromptUiApplication;
import topicpromptui.ui.viewmodel.InteractionItem;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.assertj.core.api.SoftAssertions;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.ExecutionException;

public abstract class BaseTopicPromptUiTest extends ApplicationTest {
    private final TopicPromptUiApplication app = new TopicPromptUiApplication(Modules.override(new RootModule()).with(new TestRootModule()));
    protected final StateModel stateModel = app.getGuiceContext().getInstance(StateModel.class);
    protected final MockOpenAiApi gptApi = app.getGuiceContext().getInstance(MockOpenAiApi.class);
    protected final MockGcpApi gcpApi = app.getGuiceContext().getInstance(MockGcpApi.class);
    protected final MockClaudeApi claudeApi = app.getGuiceContext().getInstance(MockClaudeApi.class);
    protected final StorageModel storage = app.getGuiceContext().getInstance(StorageModel.class);
    protected final HistorySearchModel search = app.getGuiceContext().getInstance(HistorySearchModel.class);
    protected final ClipboardModel clipboardModel = app.getGuiceContext().getInstance(ClipboardModel.class);
    private final HistoryInfo history = new HistoryInfo();
    private final TopicInfo topic = new TopicInfo();
    private final QuestionInfo question = new QuestionInfo();
    private final AnswerInfo answerGrammar = new AnswerInfo("#grammarAnswer");
    private final AnswerInfo answerOpenAi = new AnswerInfo("#openAiAnswer");
    private final AnswerInfo answerClaude = new AnswerInfo("#claudeAnswer");
    private final AnswerInfo answerGcp = new AnswerInfo("#gcpAnswer");

    @Override
    public void start(Stage stage) throws Exception {
        app.start(stage);
    }

    protected Scene scene() {
        return topic().comboBox().getScene();
    }

    protected HistoryInfo history() {
        return history;
    }

    protected TopicInfo topic() {
        return topic;
    }

    protected QuestionInfo question() {
        return question;
    }

    protected AnswerInfo grammarAnswer() {
        return answerGrammar;
    }

    protected AnswerInfo openAiAnswer() {
        return answerOpenAi;
    }

    protected AnswerInfo claudeAnswer() {
        return answerClaude;
    }

    protected AnswerInfo gcpAnswer() {
        return answerGcp;
    }

    private String extractWebViewContent(WebView webView) {
        return (String) webView.getEngine().executeScript("document.documentElement.outerHTML");
    }

    @SuppressWarnings("UnusedReturnValue")
    protected FxRobot overWrite(String text) {
        interact(() -> question().textArea().clear());
        return write(text);
    }

    void verifyWebViewBody(SoftAssertions soft, String as, WebView webView, String expContent) {
        interact(() -> soft.assertThat(extractWebViewContent(webView)).as(as)
                .isEqualTo("<html><head></head><body>" + expContent + "</body></html>"));
    }

    protected void executeSyncInFxThread(Runnable runnable) {
        try {
            WaitForAsyncUtils.asyncFx(runnable).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected WindowAssertion assertion() {
        return WindowAssertion.builder().app(this);
    }

    protected class HistoryInfo {
        public Label label() {
            return lookup("#historyLabel").queryAs(Label.class);
        }

        public ComboBox<InteractionItem> comboBox() {
            return lookup("#historyComboBox").queryComboBox();
        }

        public TextField filterTextField() {
            return lookup("#historyFilterTextField").queryAs(TextField.class);
        }

        public Label filterClearButton() {
            return lookup("#historyFilterClearButton").queryAs(Label.class);
        }

        public Button deleteButton() {
            return lookup("#historyDeleteButton").queryButton();
        }
    }

    protected class TopicInfo {
        public Label label() {
            return lookup("#topicLabel").queryAs(Label.class);
        }

        public ComboBox<Topic> comboBox() {
            return lookup("#topicComboBox").queryComboBox();
        }

        public Node comboBoxNarrow() {
            return comboBox().lookup(".arrow-button");
        }

        /** The SearchableComboBox skin's internal filtered combo box; exists only after the skin is created. */
        public ComboBox<Topic> popupFilteredComboBox() {
            return lookup("#filtered").queryComboBox();
        }

        public Button addTopicButton() {
            return lookup("#addButton").queryButton();
        }

        public Button renameButton() {
            return lookup("#renameButton").queryButton();
        }

        public Button deleteButton() {
            return lookup("#deleteButton").queryButton();
        }

        public CheckBox filterHistoryCheckBox() {
            return lookup("#filterHistoryCheckBox").queryAs(CheckBox.class);
        }
    }

    protected class QuestionInfo {
        public Label label() {
            return lookup("#questionLabel").queryAs(Label.class);
        }

        public TextArea textArea() {
            return lookup("#questionTextArea").queryAs(TextArea.class);
        }

        public Button questionButton() {
            return lookup("#questionButton").queryButton();
        }

        public Button definitionButton() {
            return lookup("#definitionButton").queryButton();
        }

        public Button grammarButton() {
            return lookup("#grammarButton").queryButton();
        }

        public Button factButton() {
            return lookup("#factButton").queryButton();
        }

        public Button regenerateButton() {
            return lookup("#regenerateButton").queryButton();
        }

        public CheckBox followUpCheckBox() {
            return lookup("#followUpCheckBox").queryAs(CheckBox.class);
        }
    }

    protected class AnswerInfo {
        private final String tag;

        public AnswerInfo(String tag) {
            this.tag = tag;
        }

        public Button button() {
            return lookup(tag + " #answerButton").queryButton();
        }

        public Button copyButton() {
            return lookup(tag + " #copyButton").queryButton();
        }

        public Button regenerateButton() {
            return lookup(tag + " #regenerateButton").queryButton();
        }

        public Button expandButton() {
            return lookup(tag + " #expandButton").queryButton();
        }

        /** The pane's root node (the fx:include root in TopicPromptUi.fxml). */
        public HBox pane() {
            return lookup(tag).queryAs(HBox.class);
        }

        public WebView webView() {
            return lookup(tag + " #webView").queryAs(WebView.class);
        }

        public Circle circle() {
            return lookup(tag + " #statusCircle").queryAs(Circle.class);
        }
    }

    protected AnswerDetailsDialog answerDetailsDialog() {
        return new AnswerDetailsDialog();
    }

    protected class AnswerDetailsDialog {
        public TextField answerTypeField() {
            return lookup("#answerTypeField").queryAs(TextField.class);
        }

        public TextField modelIdField() {
            return lookup("#modelIdField").queryAs(TextField.class);
        }

        public TextField effortLevelField() {
            return lookup("#effortLevelField").queryAs(TextField.class);
        }

        public TextField finishReasonField() {
            return lookup("#finishReasonField").queryAs(TextField.class);
        }

        public TextField inputTokensField() {
            return lookup("#inputTokensField").queryAs(TextField.class);
        }

        public TextField outputTokensField() {
            return lookup("#outputTokensField").queryAs(TextField.class);
        }

        public TextField totalTokensField() {
            return lookup("#totalTokensField").queryAs(TextField.class);
        }

        public TextArea promptArea() {
            return lookup("#promptArea").queryAs(TextArea.class);
        }
    }
}
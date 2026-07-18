package topicpromptui.ui.view;

import topicpromptui.ui.viewmodel.answer.AnswerDetails;
import topicpromptui.ui.viewmodel.answer.AnswerVmController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.util.Objects;

import static topicpromptui.core.util.LogUtils.shorten;
import static javafx.scene.input.KeyCode.DIGIT1; // NOSONAR - used in switch case labels below, S1128 false positive
import static javafx.scene.input.KeyCode.DIGIT2; // NOSONAR - used in switch case labels below, S1128 false positive
import static javafx.scene.input.KeyCode.DIGIT3; // NOSONAR - used in switch case labels below, S1128 false positive
import static javafx.scene.input.KeyCode.DIGIT4; // NOSONAR - used in switch case labels below, S1128 false positive
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.F;
import static javafx.scene.input.KeyCode.UP;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;

public class AnswerController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(AnswerController.class);
    @FXML
    private Button answerButton;
    @FXML
    private Circle statusCircle;
    @FXML
    private WebView webView;
    @FXML
    private Button copyButton;
    private AnswerVmController vm;

    @FXML
    void clickCopyButton(ActionEvent ignoredEvent) {
        log.trace("clickCopyButton");
        vm.onCopyButtonClick();
    }

    @FXML
    void onRegenerateButtonClick(ActionEvent ignoredEvent) {
        log.trace("onRegenerateButtonClick");
        vm.onRegenerateButtonClick();
    }

    @FXML
    void onExpandButtonClick(ActionEvent ignoredEvent) {
        log.trace("onExpandButtonClick");
        vm.onExpandButtonClick();
    }

    @FXML
    void onAnswerButtonClick(ActionEvent ignoredEvent) {
        log.trace("onAnswerButtonClick");
        showAnswerInfoDialog(vm.getAnswerDetails());
    }

    void initializeController(AnswerVmController vm) {
        log.trace("initializeController");
        this.vm = vm;
        webView.getEngine().documentProperty().addListener((_, _, newValue) -> onDocumentChanged(newValue));
        vm.properties().webViewContent.addListener((_, _, newValue) -> onWebViewContentChanged(newValue));
        vm.properties().statusCircleFill.bindBidirectional(statusCircle.fillProperty());
        vm.properties().answerButtonText.bindBidirectional(answerButton.textProperty());
        vm.properties().copyButtonText.bindBidirectional(copyButton.textProperty());
        webView.addEventFilter(KEY_PRESSED, this::onWebViewKeyPressed);
    }

    private void showAnswerInfoDialog(AnswerDetails details) {
        var dialog = new Dialog<Void>();
        dialog.setTitle(details.answerType() + " answer info");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        var grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(8));
        addInfoRow(grid, 0, "answerTypeField", "Answer type:", String.valueOf(details.answerType()));
        addInfoRow(grid, 1, "modelIdField", "Model ID:", details.modelId());
        addInfoRow(grid, 2, "effortLevelField", "Effort level:", details.effortLevel());
        addInfoRow(grid, 3, "finishReasonField", "Finish reason:", details.finishReason());
        addInfoRow(grid, 4, "inputTokensField", "Input tokens:", Objects.toString(details.inputTokens(), ""));
        addInfoRow(grid, 5, "outputTokensField", "Output tokens:", Objects.toString(details.outputTokens(), ""));
        addInfoRow(grid, 6, "totalTokensField", "Total tokens:", Objects.toString(details.totalTokens(), ""));

        var promptArea = new TextArea(Objects.toString(details.prompt(), ""));
        promptArea.setId("promptArea");
        promptArea.setEditable(false);
        promptArea.setWrapText(true);
        promptArea.setPrefRowCount(10);
        promptArea.setPrefColumnCount(60);
        grid.add(new Label("Prompt:"), 0, 7);
        grid.add(promptArea, 1, 7);

        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
    }

    private void addInfoRow(GridPane grid, int row, String fieldId, String labelText, String value) {
        var field = new TextField(Objects.toString(value, ""));
        field.setId(fieldId);
        field.setEditable(false);
        grid.add(new Label(labelText), 0, row);
        grid.add(field, 1, row);
    }

    private void onDocumentChanged(Document newValue) {
        if (newValue == null) {
            return;
        }
        var currentContent = vm.properties().webViewContent.getValue();
        var newContent = (String) webView.getEngine().executeScript("document.documentElement.outerHTML");
        if (!newContent.equals(currentContent)) {
            if (log.isTraceEnabled()) {
                log.trace("Set value to webViewContent from WebView Engine: {}", shorten(newContent));
            }
            vm.properties().webViewContent.set(newContent);
        }
    }

    private void onWebViewContentChanged(String newValue) {
        if (newValue == null) {
            return;
        }
        if (log.isTraceEnabled()) {
            log.trace("Load content to WebView Engine: {}", shorten(newValue));
        }
        webView.getEngine().loadContent(newValue);
    }

    // Scene accelerators don't fire while the WebView has focus (WebView consumes key events),
    // so the hotkeys that must work while reading an answer are re-dispatched from this filter.
    private void onWebViewKeyPressed(KeyEvent event) {
        if (!event.isControlDown()) {
            return;
        }
        if (!event.isAltDown()) {
            if (event.getCode() == F) {
                event.consume();
                vm.ctrlFHotkeyPressed();
                return;
            }
            var digit = switch (event.getCode()) {
                case DIGIT1 -> 1;
                case DIGIT2 -> 2;
                case DIGIT3 -> 3;
                case DIGIT4 -> 4;
                default -> 0;
            };
            if (digit != 0) {
                event.consume();
                vm.ctrlDigitHotkeyPressed(digit);
            }
            return;
        }
        if (event.getCode() == DOWN) {
            event.consume();
            vm.ctrlAltDownHotkeyPressed();
        }
        if (event.getCode() == UP) {
            event.consume();
            vm.ctrlAltUpHotkeyPressed();
        }
    }

    @Override
    protected void initialize() {
        // Intentionally empty: unlike the other controllers, AnswerController is instantiated 4 times by
        // FXML (one per AnswerType) and can't be wired to its AnswerVmController here, since FXML calls
        // this with no way to pass which of the 4 VMs to use. Real setup happens in initializeController(vm),
        // called explicitly by TopicPromptUiController once it knows which VM belongs to which pane.
    }

}

package topicpromptui.ui.view;

import com.google.gson.Gson;
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

import java.util.List;
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
    // Used only to build JS string literals (escapes quotes, <, >, & and U+2028/U+2029)
    private static final Gson GSON = new Gson();
    @FXML
    private Button answerButton;
    @FXML
    private Circle statusCircle;
    @FXML
    private WebView webView;
    @FXML
    private Button copyButton;
    private AnswerVmController vm;
    // Reentrancy guard: replaceBodyInPlace writes the normalized outerHTML back into
    // webViewContent from inside the property's own change listener; without the guard that
    // nested write would re-fire onWebViewContentChanged and loadContent would reset the scroll.
    private boolean readingBackFromEngine;

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
        var interactionIdField = new TextField(
                details.interactionId() == null ? "" : Objects.toString(details.interactionId().id(), ""));
        interactionIdField.setId("interactionIdField");
        interactionIdField.setEditable(false);
        grid.add(new Label("Interaction ID:"), 0, 0);
        grid.add(interactionIdField, 1, 0);

        var openInteractionFileButton = new Button("…");
        openInteractionFileButton.setId("openInteractionFileButton");
        openInteractionFileButton.setDisable(details.interactionId() == null);
        openInteractionFileButton.setOnAction(_ -> vm.onOpenInteractionFileButtonClick());
        grid.add(openInteractionFileButton, 2, 0);

        addInfoRow(grid, 1, "answerTypeField", "Answer type:", String.valueOf(details.answerType()));
        addInfoRow(grid, 2, "modelIdField", "Model ID:", details.modelId());
        addInfoRow(grid, 3, "effortLevelField", "Effort level:", details.effortLevel());
        addInfoRow(grid, 4, "finishReasonField", "Finish reason:", details.finishReason());
        addInfoRow(grid, 5, "inputTokensField", "Input tokens:", Objects.toString(details.inputTokens(), ""));
        addInfoRow(grid, 6, "outputTokensField", "Output tokens:", Objects.toString(details.outputTokens(), ""));
        addInfoRow(grid, 7, "totalTokensField", "Total tokens:", Objects.toString(details.totalTokens(), ""));

        var toolsUsedArea = new TextArea(formatToolCalls(details.toolCalls()));
        toolsUsedArea.setId("toolsUsedArea");
        toolsUsedArea.setEditable(false);
        toolsUsedArea.setWrapText(true);
        toolsUsedArea.setPrefRowCount(3);
        toolsUsedArea.setPrefColumnCount(60);
        grid.add(new Label("Tools used:"), 0, 8);
        grid.add(toolsUsedArea, 1, 8);

        var systemPromptArea = new TextArea(Objects.toString(details.systemPrompt(), ""));
        systemPromptArea.setId("systemPromptArea");
        systemPromptArea.setEditable(false);
        systemPromptArea.setWrapText(true);
        systemPromptArea.setPrefRowCount(10);
        systemPromptArea.setPrefColumnCount(60);
        grid.add(new Label("System prompt:"), 0, 9);
        grid.add(systemPromptArea, 1, 9);

        var promptArea = new TextArea(Objects.toString(details.prompt(), ""));
        promptArea.setId("promptArea");
        promptArea.setEditable(false);
        promptArea.setWrapText(true);
        promptArea.setPrefRowCount(10);
        promptArea.setPrefColumnCount(60);
        grid.add(new Label("Prompt:"), 0, 10);
        grid.add(promptArea, 1, 10);

        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
    }

    private static String formatToolCalls(List<String> toolCalls) {
        return toolCalls == null ? "" : String.join("\n", toolCalls);
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
        if (newValue == null || readingBackFromEngine) {
            return;
        }
        if (vm.properties().preserveScrollOnNextUpdate && canReplaceBodyInPlace()) {
            if (log.isTraceEnabled()) {
                log.trace("Replace WebView body in place: {}", shorten(newValue));
            }
            replaceBodyInPlace(newValue);
        } else {
            if (log.isTraceEnabled()) {
                log.trace("Load content to WebView Engine: {}", shorten(newValue));
            }
            webView.getEngine().loadContent(newValue);
        }
    }

    private boolean canReplaceBodyInPlace() {
        var document = webView.getEngine().getDocument();
        return document != null && document.getElementsByTagName("body").getLength() > 0;
    }

    // Streaming/completion updates mutate the live body instead of reloading the document, so the
    // WebView keeps its vertical scroll position while an answer streams in.
    private void replaceBodyInPlace(String bodyHtmlFragment) {
        var engine = webView.getEngine();
        engine.executeScript("document.body.innerHTML = " + GSON.toJson(bodyHtmlFragment) + ";");
        // Keep the load-path invariant (see onDocumentChanged): webViewContent mirrors the
        // engine's normalized outerHTML after the DOM settles — the Copy button depends on it.
        var outerHtml = (String) engine.executeScript("document.documentElement.outerHTML");
        readingBackFromEngine = true;
        try {
            vm.properties().webViewContent.set(outerHtml);
        } finally {
            readingBackFromEngine = false;
        }
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

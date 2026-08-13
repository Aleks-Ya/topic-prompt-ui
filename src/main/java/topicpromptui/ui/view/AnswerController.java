package topicpromptui.ui.view;

import com.google.gson.Gson;
import topicpromptui.ui.viewmodel.answer.AnswerVmController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.UncheckedIOException;

import static topicpromptui.core.util.LogUtils.shorten;
import static topicpromptui.core.util.ResourceUtils.resourceUrl;
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
        // A plain loader, not the Guice-provided one: FXMLLoader is bound only by Ignite's GuiceContext
        // when the app boots, so injecting it here would break every test that builds a bare RootModule
        // injector (requireExplicitBindings). AnswerDetailsDialogController needs nothing from Guice, so
        // FXMLLoader's default reflective instantiation is enough. A fresh loader per open, since a
        // loader keeps the root/controller of its last load and cannot be reused.
        var loader = new FXMLLoader(resourceUrl(getClass(), "/topicpromptui/ui/view/AnswerDetailsDialog.fxml"));
        try {
            loader.load();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        AnswerDetailsDialogController dialogController = loader.getController();
        dialogController.showDialog(vm.getAnswerDetails(), vm::onOpenInteractionFileButtonClick);
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

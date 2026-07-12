package gptui.ui.view;

import gptui.ui.viewmodel.answer.AnswerVmController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static gptui.core.util.LogUtils.shorten;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.UP;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;

public class AnswerController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(AnswerController.class);
    @FXML
    private Label answerLabel;
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

    void initializeController(AnswerVmController vm) {
        log.trace("initializeController");
        this.vm = vm;
        webView.getEngine().documentProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                var currentContent = vm.properties().webViewContent.getValue();
                var newContent = (String) webView.getEngine().executeScript("document.documentElement.outerHTML");
                if (!newContent.equals(currentContent)) {
                    log.trace("Set value to webViewContent from WebView Engine: {}", shorten(newContent));
                    vm.properties().webViewContent.set(newContent);
                }
            }
        });
        vm.properties().webViewContent.addListener((_, _, newValue) -> {
            if (newValue != null) {
                log.trace("Load content to WebView Engine: {}", shorten(newValue));
                webView.getEngine().loadContent(newValue);
            }
        });
        vm.properties().statusCircleFill.bindBidirectional(statusCircle.fillProperty());
        vm.properties().answerLabelText.bindBidirectional(answerLabel.textProperty());
        vm.properties().copyButtonText.bindBidirectional(copyButton.textProperty());
        webView.addEventFilter(KEY_PRESSED, event -> {
            if (event.isControlDown() && event.isAltDown()) {
                if (event.getCode() == DOWN) {
                    event.consume();
                    vm.ctrlAltDownHotkeyPressed();
                }
                if (event.getCode() == UP) {
                    event.consume();
                    vm.ctrlAltUpHotkeyPressed();
                }
            }
        });
    }

    @Override
    protected void initialize() {
        // Intentionally empty: unlike the other controllers, AnswerController is instantiated 4 times by
        // FXML (one per AnswerType) and can't be wired to its AnswerVmController here, since FXML calls
        // this with no way to pass which of the 4 VMs to use. Real setup happens in initializeController(vm),
        // called explicitly by GptUiController once it knows which VM belongs to which pane.
    }

}

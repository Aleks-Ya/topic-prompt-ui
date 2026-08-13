package topicpromptui.ui.view;

import topicpromptui.ui.viewmodel.question.QuestionVmController;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FXMLLoader instantiates this controller itself, so only member injection is possible; NOSONAR
// see TopicPromptUiController for the full rationale.
@SuppressWarnings("java:S6813")
public class QuestionController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(QuestionController.class);
    @Inject
    private QuestionVmController vm;
    @FXML
    private TextArea questionTextArea;
    @FXML
    private CheckBox followUpCheckBox;

    @FXML
    void sendQuestion(ActionEvent ignoredEvent) {
        log.debug("sendQuestion");
        vm.onSendQuestionClick();
    }

    @FXML
    void sendDefinition(ActionEvent ignoredEvent) {
        log.debug("sendDefinition");
        vm.onSendDefinitionClick();
    }

    @FXML
    void sendGrammar(ActionEvent ignoredEvent) {
        log.debug("sendGrammar");
        vm.onSendGrammarClick();
    }

    @FXML
    public void sendFact(ActionEvent ignoredEvent) {
        log.debug("sendFact");
        vm.onSendFactClick();
    }

    @FXML
    void onRegenerateButtonClick(ActionEvent ignoredEvent) {
        log.trace("onRegenerateButtonClick");
        vm.onRegenerateButtonClick();
    }

    @FXML
    void keyTypedQuestionTextArea(KeyEvent ignoredEvent) {
        log.trace("keyTypedQuestionTextArea");
        vm.onKeyTypedQuestionTextArea();
    }

    private void applyQuestionStyleClass(String oldClass, String newClass) {
        if (oldClass != null) {
            questionTextArea.getStyleClass().remove(oldClass);
        }
        if (newClass != null) {
            questionTextArea.getStyleClass().add(newClass);
        }
    }

    @Override
    protected void initialize() {
        vm.properties().followUpCheckBoxSelected.bindBidirectional(followUpCheckBox.selectedProperty());
        vm.properties().questionTaText.bindBidirectional(questionTextArea.textProperty());
        // A style class can't be bound like a property, so swap it by hand. One-way is enough: the
        // view model is the only writer.
        applyQuestionStyleClass(null, vm.properties().questionTaStyle.get());
        vm.properties().questionTaStyle.addListener((_, oldClass, newClass) -> applyQuestionStyleClass(oldClass, newClass));
        vm.properties().questionTaFocused.addListener((observable, oldValue, newValue) -> {
                    if (Boolean.TRUE.equals(newValue)) {
                        Platform.runLater(() -> questionTextArea.requestFocus());
                    }
                }
        );
        vm.properties().questionTaSelectAll.addListener((observable, oldValue, newValue) -> {
                    if (Boolean.TRUE.equals(newValue)) {
                        Platform.runLater(() -> questionTextArea.selectAll());
                    }
                }
        );
        vm.properties().questionTaPositionCaretToEnd.addListener((observable, oldValue, newValue) -> {
                    if (Boolean.TRUE.equals(newValue)) {
                        Platform.runLater(() -> questionTextArea.positionCaret(questionTextArea.getText().length()));
                    }
                }
        );
    }
}


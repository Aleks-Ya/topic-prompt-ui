package topicpromptui.ui.view;

import topicpromptui.ui.viewmodel.answer.AnswerDetails;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

// Unlike the other controllers this one is not Guice-bound: it has no injected collaborators, so
// AnswerController loads it with a plain FXMLLoader (see the rationale there).
public class AnswerDetailsDialogController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(AnswerDetailsDialogController.class);
    @FXML
    private DialogPane dialogPane;
    @FXML
    private TextField interactionIdField;
    @FXML
    private Button openInteractionFileButton;
    @FXML
    private TextField answerTypeField;
    @FXML
    private TextField modelIdField;
    @FXML
    private TextField effortLevelField;
    @FXML
    private TextField finishReasonField;
    @FXML
    private TextField inputTokensField;
    @FXML
    private TextField outputTokensField;
    @FXML
    private TextField totalTokensField;
    @FXML
    private TextArea toolsUsedArea;
    @FXML
    private TextArea systemPromptArea;
    @FXML
    private TextArea promptArea;
    @FXML
    private TextArea answerMdArea;
    @FXML
    private TextArea answerHtmlArea;
    private Runnable onOpenInteractionFile;

    void showDialog(AnswerDetails details, Runnable onOpenInteractionFile) {
        log.trace("showDialog: {}", details.answerType());
        this.onOpenInteractionFile = onOpenInteractionFile;

        interactionIdField.setText(details.interactionId() == null
                ? "" : Objects.toString(details.interactionId().id(), ""));
        openInteractionFileButton.setDisable(details.interactionId() == null);
        answerTypeField.setText(String.valueOf(details.answerType()));
        modelIdField.setText(Objects.toString(details.modelId(), ""));
        effortLevelField.setText(Objects.toString(details.effortLevel(), ""));
        finishReasonField.setText(Objects.toString(details.finishReason(), ""));
        inputTokensField.setText(Objects.toString(details.inputTokens(), ""));
        outputTokensField.setText(Objects.toString(details.outputTokens(), ""));
        totalTokensField.setText(Objects.toString(details.totalTokens(), ""));
        toolsUsedArea.setText(formatToolCalls(details.toolCalls()));
        systemPromptArea.setText(Objects.toString(details.systemPrompt(), ""));
        promptArea.setText(Objects.toString(details.prompt(), ""));
        answerMdArea.setText(Objects.toString(details.answerMd(), ""));
        answerHtmlArea.setText(Objects.toString(details.answerHtml(), ""));

        var dialog = new Dialog<Void>();
        dialog.setDialogPane(dialogPane);
        dialog.setTitle(details.answerType() + " answer info");
        dialog.setResizable(true);
        dialog.showAndWait();
    }

    @FXML
    void onOpenInteractionFileButtonClick(ActionEvent ignoredEvent) {
        log.trace("onOpenInteractionFileButtonClick");
        onOpenInteractionFile.run();
    }

    private static String formatToolCalls(List<String> toolCalls) {
        return toolCalls == null ? "" : String.join("\n", toolCalls);
    }

    @Override
    protected void initialize() {
        // Intentionally empty: every control is populated per-open by showDialog(details, ...), since the
        // same controller instance is created fresh for each dialog and has no VM to bind to.
    }

}

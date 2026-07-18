package topicpromptui.ui.view;

import topicpromptui.ui.viewmodel.InteractionItem;
import topicpromptui.ui.viewmodel.history.HistoryVmController;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.controlsfx.control.textfield.CustomTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FXMLLoader instantiates this controller itself, so only member injection is possible; NOSONAR
// see TopicPromptUiController for the full rationale.
@SuppressWarnings("java:S6813")
public class HistoryController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(HistoryController.class);
    @FXML
    private Label historyLabel;
    @FXML
    private Label historyLabelTemplate;
    @FXML
    private ComboBox<InteractionItem> historyComboBox;
    @FXML
    private CustomTextField historyFilterTextField;
    @FXML
    private Button historyDeleteButton;
    @Inject
    private HistoryVmController vm;

    @FXML
    void historyComboBoxAction(ActionEvent ignoredEvent) {
        log.trace("historyComboBoxAction");
        vm.onHistoryComboBoxAction();
    }

    @FXML
    void clickHistoryDeleteButton(ActionEvent ignoredEvent) {
        log.trace("clickHistoryDeleteButton");
        vm.onClickHistoryDeleteButton();
    }

    @Override
    protected void initialize() {
        vm.properties().historyLabelText.bindBidirectional(historyLabel.textProperty());
        vm.properties().historyLabelTemplateText.bindBidirectional(historyLabelTemplate.textProperty());
        vm.properties().historyCbSelectionModel.bindBidirectional(historyComboBox.selectionModelProperty());
        vm.properties().historyCbItems.bindBidirectional(historyComboBox.itemsProperty());
        vm.properties().historyCbOnAction.bindBidirectional(historyComboBox.onActionProperty());
        vm.properties().historyFilterTfText.bindBidirectional(historyFilterTextField.textProperty());
        vm.properties().historyFilterTfFocused.addListener((observable, oldValue, newValue) -> {
                    if (Boolean.TRUE.equals(newValue)) {
                        Platform.runLater(() -> historyFilterTextField.requestFocus());
                    }
                }
        );
        vm.properties().historyFilterTfSelectAll.addListener((observable, oldValue, newValue) -> {
                    if (Boolean.TRUE.equals(newValue)) {
                        Platform.runLater(() -> historyFilterTextField.selectAll());
                    }
                }
        );
        vm.properties().historyDeleteButtonDisable.bindBidirectional(historyDeleteButton.disableProperty());
        historyFilterTextField.setRight(createFilterClearButton());
    }

    private Label createFilterClearButton() {
        var clearButton = new Label("✕");
        clearButton.setId("historyFilterClearButton");
        clearButton.setCursor(Cursor.HAND);
        clearButton.setPadding(new Insets(0, 4, 0, 0));
        clearButton.setOnMouseClicked(event -> historyFilterTextField.clear());
        clearButton.visibleProperty().bind(historyFilterTextField.textProperty().isNotEmpty());
        return clearButton;
    }
}


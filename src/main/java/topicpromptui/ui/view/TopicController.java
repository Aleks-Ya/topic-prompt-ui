package topicpromptui.ui.view;

import topicpromptui.core.storagefilesystem.Topic;
import topicpromptui.ui.viewmodel.topic.TopicVmController;
import jakarta.inject.Inject;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import org.controlsfx.control.SearchableComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// FXMLLoader instantiates this controller itself, so only member injection is possible; NOSONAR
// see TopicPromptUiController for the full rationale.
@SuppressWarnings("java:S6813")
public class TopicController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(TopicController.class);
    @FXML
    private Label topicLabel;
    @FXML
    private SearchableComboBox<Topic> topicComboBox;
    @FXML
    private CheckBox filterHistoryCheckBox;
    @FXML
    private Button addButton;
    @FXML
    private Button renameButton;
    @FXML
    private Button deleteButton;
    @Inject
    private TopicVmController vm;
    private final TextInputDialog newTopicDialog = new TextInputDialog();
    private final TextInputDialog renameTopicDialog = new TextInputDialog();
    private final Alert deleteTopicDialog = new Alert(Alert.AlertType.CONFIRMATION);

    @FXML
    void topicFilterHistoryCheckBoxClicked(ActionEvent ignore) {
        log.trace("topicFilterHistoryCheckBoxClicked");
        vm.onTopicFilterHistoryCheckBoxClicked();
    }

    @FXML
    void onAddButtonClicked(ActionEvent ignore) {
        log.trace("onAddButtonClicked");
        vm.onTopicFilterHistoryCheckBoxClicked();
    }

    @Override
    protected void initialize() {
        vm.properties().topicLabelText.bindBidirectional(topicLabel.textProperty());
        vm.properties().topicCbValue.bindBidirectional(topicComboBox.valueProperty());
        vm.properties().topicCbItems.bindBidirectional(topicComboBox.itemsProperty());
        vm.properties().topicCbEditor.bindBidirectional(topicComboBox.getEditor().textProperty());
        vm.properties().topicCbCellFactory.bindBidirectional(topicComboBox.cellFactoryProperty());
        vm.properties().filterHistoryCheckBoxSelected.bindBidirectional(filterHistoryCheckBox.selectedProperty());
        vm.properties().renameButtonDisable.bindBidirectional(renameButton.disableProperty());

        newTopicDialog.setTitle("Add new topic");
        newTopicDialog.setHeaderText("New topic:");
        newTopicDialog.getDialogPane().lookupButton(ButtonType.OK)
                .disableProperty().bind(Bindings.createBooleanBinding(
                        () -> newTopicDialog.getEditor().getText().isBlank(),
                        newTopicDialog.getEditor().textProperty()));
        addButton.setOnAction(_ -> {
            newTopicDialog.show();
            newTopicDialog.getEditor().clear();
            newTopicDialog.getEditor().requestFocus();
            newTopicDialog.hide();
            newTopicDialog.showAndWait().ifPresent(topic -> vm.addNewTopic(topic));
        });

        renameTopicDialog.setTitle("Rename topic");
        renameTopicDialog.setHeaderText("New topic name:");
        renameTopicDialog.getDialogPane().lookupButton(ButtonType.OK)
                .disableProperty().bind(Bindings.createBooleanBinding(
                        () -> renameTopicDialog.getEditor().getText().isBlank(),
                        renameTopicDialog.getEditor().textProperty()));
        renameButton.setOnAction(_ -> {
            var currentTopic = topicComboBox.getValue();
            renameTopicDialog.show();
            renameTopicDialog.getEditor().setText(currentTopic != null ? currentTopic.title() : "");
            renameTopicDialog.getEditor().selectAll();
            renameTopicDialog.getEditor().requestFocus();
            renameTopicDialog.hide();
            renameTopicDialog.showAndWait().ifPresent(newTitle -> vm.renameCurrentTopic(newTitle));
        });

        vm.properties().deleteButtonDisable.bindBidirectional(deleteButton.disableProperty());
        deleteTopicDialog.setTitle("Delete topic");
        ((Button) deleteTopicDialog.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(false);
        ((Button) deleteTopicDialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton(true);
        deleteButton.setOnAction(_ -> {
            var currentTopic = topicComboBox.getValue();
            deleteTopicDialog.setHeaderText("Delete topic \"" + currentTopic.title() + "\"?");
            deleteTopicDialog.setContentText("This will also delete " + vm.getInteractionCountInCurrentTopic()
                    + " interaction(s) in this topic.");
            deleteTopicDialog.showAndWait()
                    .filter(buttonType -> buttonType == ButtonType.OK)
                    .ifPresent(_ -> vm.deleteCurrentTopic());
        });
        topicLabel.setLabelFor(topicComboBox);
        topicComboBox.showingProperty()
                .addListener((_, _, _) -> vm.onTopicComboBoxAction());
        SearchableComboBoxShortestFirst.attach(topicComboBox);
    }
}

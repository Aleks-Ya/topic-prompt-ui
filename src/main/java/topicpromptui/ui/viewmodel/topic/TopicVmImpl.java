package topicpromptui.ui.viewmodel.topic;

import com.google.inject.Singleton;
import topicpromptui.core.storagefilesystem.Interaction;
import topicpromptui.core.storagefilesystem.Topic;
import topicpromptui.ui.viewmodel.mediator.TopicMediator;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Singleton
class TopicVmImpl implements TopicVmController, TopicVmMediator {
    private static final Logger log = LoggerFactory.getLogger(TopicVmImpl.class);
    public final TopicVmProperties vmProperties = new TopicVmProperties();
    private final TopicMediator mediator;

    @Inject
    TopicVmImpl(TopicMediator mediator) {
        this.mediator = mediator;
    }

    @Override
    public void onTopicComboBoxAction() {
        log.trace("onTopicComboBoxAction");
        chooseTopicFromCb();
    }

    @Override
    public void onTopicFilterHistoryCheckBoxClicked() {
        log.trace("onTopicFilterHistoryCheckBoxClicked");
        var cbValue = vmProperties.filterHistoryCheckBoxSelected.getValue();
        var modelValue = mediator.isHistoryFilteringEnabled();
        log.trace("cbValue={}, modelValue={}", cbValue, modelValue);
        if (!Objects.equals(cbValue, modelValue)) {
            log.trace("Setting TopicFilterHistoryCheckBox to {}", cbValue);
            mediator.setIsHistoryFilteringEnabled(cbValue);
            mediator.isTopicFilterHistoryChanged();
        }
    }

    @Override
    public TopicVmProperties properties() {
        return vmProperties;
    }

    @Override
    public void addNewTopic(String topic) {
        log.trace("addNewTopic");
        var newTopic = mediator.addTopic(topic);
        mediator.setCurrentTopic(newTopic);
        mediator.topicWasChosen();
    }

    @Override
    public void renameCurrentTopic(String newTitle) {
        log.trace("renameCurrentTopic");
        var currentTopic = mediator.getCurrentTopic();
        var renamedOrTargetTopic = mediator.renameTopic(currentTopic.id(), newTitle);
        mediator.setCurrentTopic(renamedOrTargetTopic);
        mediator.topicWasChosen();
    }

    @Override
    public void deleteCurrentTopic() {
        log.trace("deleteCurrentTopic");
        var currentTopic = mediator.getCurrentTopic();
        mediator.deleteTopic(currentTopic.id());
        mediator.topicWasChosen();
    }

    @Override
    public Long getInteractionCountInCurrentTopic() {
        return mediator.getInteractionCountInTopic(mediator.getCurrentTopic().title());
    }

    @Override
    public void updateComboBoxSelectedItemFromCurrentInteraction() {
        var topicTitle = mediator.getCurrentInteractionOpt()
                .map(Interaction::topicId)
                .map(mediator::getTopic)
                .orElse(null);
        mediator.setCurrentTopic(topicTitle);
        vmProperties.topicCbValue.setValue(topicTitle);
        updateButtonsDisable();
    }

    @Override
    public void updateComboBoxSelectedItemFromStateModel() {
        vmProperties.topicCbValue.setValue(mediator.getCurrentTopic());
        updateButtonsDisable();
    }

    private void updateButtonsDisable() {
        var noCurrentTopic = mediator.getCurrentTopic() == null;
        vmProperties.renameButtonDisable.setValue(noCurrentTopic);
        vmProperties.deleteButtonDisable.setValue(noCurrentTopic);
    }

    @Override
    public void updateComboBoxItems() {
        var currentModelItems = FXCollections.observableArrayList(mediator.getTopics());
        var currentComboBoxItems = vmProperties.topicCbItems.getValue();
        if (!Objects.equals(currentModelItems, currentComboBoxItems)) {
            log.trace("Set topicCbItems: {}", currentModelItems);
            vmProperties.topicCbItems.setValue(currentModelItems);
            setLabel();
        }
    }

    @Override
    public void setLabel() {
        vmProperties.topicLabelText.setValue(String.format("_Topic (%d):", mediator.getTopics().size()));
    }

    @Override
    public void initialize() {
        vmProperties.topicCbCellFactory.setValue(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Topic item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item + " (" + mediator.getInteractionCountInTopic(item.title()) + ")");
                }
            }
        });
    }

    private void chooseTopicFromCb() {
        log.trace("chooseTopicFromCb");
        var currentComboBoxValue = vmProperties.topicCbValue.getValue();
        log.trace("currentComboBoxValue: '{}'", currentComboBoxValue);
        var currentModelValue = mediator.getCurrentTopic();
        log.trace("currentModelValue: '{}'", currentModelValue);
        if (currentComboBoxValue != null && !Objects.equals(currentComboBoxValue, currentModelValue)) {
            mediator.setCurrentTopic(currentComboBoxValue);
            mediator.topicWasChosen();
        }
    }
}


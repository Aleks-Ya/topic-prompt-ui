package topicpromptui.ui.viewmodel.topic;

public interface TopicVmMediator {
    void updateComboBoxSelectedItemFromCurrentInteraction();

    void updateComboBoxSelectedItemFromStateModel();

    void updateComboBoxItems();

    void setLabel();

    void setTopicComboBoxDisable(boolean disable);

    void initialize();
}

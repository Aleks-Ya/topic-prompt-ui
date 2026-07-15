package gptui.ui.viewmodel.topic;

public interface TopicVmMediator {
    void updateComboBoxSelectedItemFromCurrentInteraction();

    void updateComboBoxSelectedItemFromStateModel();

    void updateComboBoxItems();

    void setLabel();

    void initialize();
}

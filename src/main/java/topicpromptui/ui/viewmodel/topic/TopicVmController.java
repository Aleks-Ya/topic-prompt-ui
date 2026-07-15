package topicpromptui.ui.viewmodel.topic;

public interface TopicVmController {
    void onTopicComboBoxAction();

    void onTopicFilterHistoryCheckBoxClicked();

    void addNewTopic(String topic);

    void renameCurrentTopic(String newTitle);

    TopicVmProperties properties();
}

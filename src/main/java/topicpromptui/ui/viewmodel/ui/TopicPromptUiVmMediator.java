package topicpromptui.ui.viewmodel.ui;

import topicpromptui.core.storagefilesystem.AnswerType;

public interface TopicPromptUiVmMediator {
    void toggleExpandedAnswer(AnswerType answerType);

    boolean isAnswerExpanded();

    void collapseExpandedAnswer();
}

package topicpromptui.ui.viewmodel.mediator;

import topicpromptui.core.storagefilesystem.AnswerType;
import topicpromptui.core.storagefilesystem.Interaction;
import topicpromptui.core.storagefilesystem.InteractionId;

import java.util.Optional;

public interface AnswerMediator {
    void selectNextHistoryItem();

    void putHtmlToClipboard(String html);

    InteractionId getCurrentInteractionId();

    Optional<Interaction> getCurrentInteractionOpt();

    void requestAnswer(InteractionId interactionId, AnswerType answerType);
}

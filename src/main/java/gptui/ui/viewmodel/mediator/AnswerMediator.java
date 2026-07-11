package gptui.ui.viewmodel.mediator;

import gptui.core.storagefilesystem.AnswerType;
import gptui.core.storagefilesystem.Interaction;
import gptui.core.storagefilesystem.InteractionId;

import java.util.Optional;

public interface AnswerMediator {
    void selectNextHistoryItem();

    void putHtmlToClipboard(String html);

    InteractionId getCurrentInteractionId();

    Optional<Interaction> getCurrentInteractionOpt();

    void requestAnswer(InteractionId interactionId, AnswerType answerType);
}

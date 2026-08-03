package topicpromptui.ui.viewmodel.mediator;

import topicpromptui.core.domain.AnswerType;
import topicpromptui.core.domain.Interaction;
import topicpromptui.core.domain.InteractionId;

import java.util.Optional;

public interface AnswerMediator {
    void selectNextHistoryItem();

    void selectPreviousHistoryItem();

    void focusHistoryFilter();

    void putHtmlToClipboard(String html);

    InteractionId getCurrentInteractionId();

    Optional<Interaction> getCurrentInteractionOpt();

    void requestAnswer(InteractionId interactionId, AnswerType answerType);

    void toggleExpandedAnswer(AnswerType answerType);

    void openInteractionFile(InteractionId interactionId);
}

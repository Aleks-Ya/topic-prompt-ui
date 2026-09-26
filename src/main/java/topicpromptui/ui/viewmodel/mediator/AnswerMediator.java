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

    /** Caption for this pane's answer button: the display name of the provider selected for the slot. */
    String getAnswerCaption(AnswerType answerType);

    void openInteractionFile(InteractionId interactionId);

    void openUrl(String url);
}

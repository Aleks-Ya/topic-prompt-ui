package topicpromptui.ui.viewmodel.mediator;

import topicpromptui.core.domain.AiProvider;
import topicpromptui.core.domain.AnswerType;
import topicpromptui.core.domain.Interaction;
import topicpromptui.core.domain.InteractionId;

import java.util.List;
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

    AiProvider getAnswerProvider(AnswerType answerType);

    /** The providers a pane may be switched to, in a stable order suitable for a selection control. */
    List<AiProvider> getAvailableProviders();

    /**
     * Points a pane at another provider and refreshes that pane. No main-code caller yet: this is the
     * seam the per-pane provider ComboBox will use.
     */
    void setAnswerProvider(AnswerType answerType, AiProvider provider);

    void openInteractionFile(InteractionId interactionId);

    void openUrl(String url);
}

package topicpromptui.ui.model.question;

import topicpromptui.core.domain.AnswerType;
import topicpromptui.core.domain.InteractionId;

import java.util.function.Consumer;

public interface QuestionModel {
    /**
     * {@code progressHtml} receives throttled partial-answer HTML snapshots on the JavaFX
     * Application Thread while the answer streams; the final answer is delivered via
     * {@code callback} and storage as before.
     */
    void requestAnswer(InteractionId interactionId, AnswerType answerType, Runnable callback,
                       Consumer<String> progressHtml);

    default void requestFollowUpAnswer(InteractionId interactionId, AnswerType answerType, Runnable callback) {
        requestFollowUpAnswer(interactionId, answerType, callback, _ -> {
        });
    }

    void requestFollowUpAnswer(InteractionId interactionId, AnswerType answerType, Runnable callback,
                               Consumer<String> progressHtml);
}

package topicpromptui.ui.model.question.prompt;

import topicpromptui.core.storagefilesystem.AnswerType;
import topicpromptui.core.storagefilesystem.InteractionType;

import java.util.Optional;

public interface PromptFactory {
    /** The user message (topic + question). Empty for the interaction {@code GRAMMAR} × non-grammar skip case. */
    Optional<String> getPrompt(InteractionType interactionType, String topic, String question, AnswerType answerType);

    /**
     * The system prompt (stable behavioral instructions, plus the conversation's topic) for the
     * given interaction/answer type, applied to every turn including follow-ups. Empty for the same
     * skip case as {@link #getPrompt}.
     */
    Optional<String> getSystemPrompt(InteractionType interactionType, String topic, AnswerType answerType);
}

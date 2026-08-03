package topicpromptui.core.prompt;

import topicpromptui.core.domain.AnswerType;
import topicpromptui.core.domain.InteractionType;

import java.util.Optional;

public interface PromptFactory {
    /** The user message (question only). Empty for the interaction {@code GRAMMAR} × non-grammar skip case. */
    Optional<String> getPrompt(InteractionType interactionType, String question, AnswerType answerType);

    /**
     * The system prompt (stable behavioral instructions, plus the conversation's topic) for the
     * given interaction/answer type, applied to every turn including follow-ups. Empty for the same
     * skip case as {@link #getPrompt}.
     */
    Optional<String> getSystemPrompt(InteractionType interactionType, String topic, AnswerType answerType);
}

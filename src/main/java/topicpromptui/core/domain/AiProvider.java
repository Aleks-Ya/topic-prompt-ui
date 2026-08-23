package topicpromptui.core.domain;

/**
 * Identity of an AI backend, independent of whether any answer pane currently uses it.
 * Every implemented provider stays listed here even while unused, so it can be reassigned to a
 * slot later without touching {@code core.ai} — see {@link AnswerType} for the assignment.
 */
public enum AiProvider {
    OPEN_AI, OPEN_AI_GRAMMAR, CLAUDE, GCP, XAI
}

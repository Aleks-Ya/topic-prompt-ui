package topicpromptui.core.domain;

/**
 * One answer pane, i.e. one slot in an {@link Interaction}'s answer map.
 * <p>
 * The constant names are <b>slot ids, not providers</b>: {@code AI_2} means "the third pane",
 * whichever {@link AiProvider} currently backs it. Reassigning a pane to another provider is a
 * one-line edit here — change the constant's provider and caption — and nothing else moves.
 * <p>
 * The names are also the persisted JSON keys of {@code Interaction.answers}, so renaming a constant
 * invalidates previously stored interactions.
 * <p>
 * The caption is a display string in the domain layer on purpose: it is the provider's visible name,
 * and keeping it apart from {@link #provider} is what makes a half-finished swap possible. The pane's
 * hotkey digit deliberately stays in {@code AnswerVmImpl} — that is pane position, not provider identity.
 */
public enum AnswerType {
    GRAMMAR(AiProvider.OPEN_AI_GRAMMAR, "Grammar:"),
    AI_1(AiProvider.OPEN_AI, "OpenAI:"),
    AI_2(AiProvider.XAI, "Grok:"),
    AI_3(AiProvider.GCP, "Gemini:");

    private final AiProvider provider;
    private final String caption;

    AnswerType(AiProvider provider, String caption) {
        this.provider = provider;
        this.caption = caption;
    }

    public AiProvider provider() {
        return provider;
    }

    public String caption() {
        return caption;
    }
}

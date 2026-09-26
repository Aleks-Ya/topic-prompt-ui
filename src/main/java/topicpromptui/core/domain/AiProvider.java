package topicpromptui.core.domain;

/**
 * Identity of an AI backend, independent of whether any answer pane currently uses it.
 * Every implemented provider stays listed here even while unused, so it can be selected for a
 * slot later without touching {@code core.ai} — the slot to provider assignment is runtime state
 * owned by {@code ui.model.aiprovider.AiProviderSelectionModel}.
 * <p>
 * {@code OPEN_AI_GRAMMAR} is not a separate vendor: it is OpenAI on a cheaper model with server-side
 * tools off, bound as a second instance in {@code OpenAiModule}.
 */
public enum AiProvider {
    OPEN_AI("OpenAI:"),
    OPEN_AI_GRAMMAR("Grammar:"),
    CLAUDE("Claude:"),
    GCP("Gemini:"),
    XAI("Grok:");

    private final String caption;

    AiProvider(String caption) {
        this.caption = caption;
    }

    /** Text on the answer button of whichever pane currently selects this provider. */
    public String caption() {
        return caption;
    }
}

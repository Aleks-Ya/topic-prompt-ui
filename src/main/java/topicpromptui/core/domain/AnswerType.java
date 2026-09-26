package topicpromptui.core.domain;

/**
 * One answer pane, i.e. one slot in an {@link Interaction}'s answer map. A slot id and nothing more:
 * {@code AI_2} means "the third pane", whichever {@link AiProvider} currently backs it.
 * <p>
 * Which provider backs a slot is not hardcoded here — it is chosen at runtime by
 * {@code ui.model.aiprovider.AiProviderSelectionModel} (hardcoded defaults, overridable per slot from
 * {@code config.properties} and via its setter), and the pane's button caption comes from
 * {@link AiProvider#displayName()} of the selected provider.
 * <p>
 * The names are the persisted JSON keys of {@code Interaction.answers}, so renaming a constant
 * invalidates previously stored interactions.
 * <p>
 * The pane's hotkey digit deliberately stays in {@code AnswerVmImpl} — that is pane position, not
 * provider identity.
 */
public enum AnswerType {
    GRAMMAR, AI_1, AI_2, AI_3
}

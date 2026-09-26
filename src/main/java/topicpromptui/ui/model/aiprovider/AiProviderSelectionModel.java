package topicpromptui.ui.model.aiprovider;

import topicpromptui.core.domain.AiProvider;
import topicpromptui.core.domain.AnswerType;

import java.util.List;
import java.util.Map;

/**
 * Which {@link AiProvider} currently backs each answer pane. Seeded from the hardcoded defaults,
 * overridden per slot by {@code ai.provider.<slot>} in {@code config.properties}, and changeable at
 * runtime through {@link #setProvider}.
 * <p>
 * Only the provider is selectable here: the model name, reasoning effort and server-side tools flag stay
 * hardcoded per binding in the {@code core.ai.*} {@code *Module}s.
 */
public interface AiProviderSelectionModel {
    AiProvider getProvider(AnswerType answerType);

    /**
     * @throws IllegalArgumentException if {@link AiProviderModule} binds no {@code AiApi} for the provider
     */
    void setProvider(AnswerType answerType, AiProvider provider);

    /** Immutable snapshot of the whole slot to provider table. */
    Map<AnswerType, AiProvider> getAll();

    /**
     * The providers a pane may be switched to: every {@link AiProvider} that {@link AiProviderModule}
     * binds an {@code AiApi} for, in enum declaration order. Includes {@code OPEN_AI_GRAMMAR}, which is
     * the default of the {@code GRAMMAR} slot and therefore must be offerable to it.
     */
    List<AiProvider> getAvailableProviders();
}

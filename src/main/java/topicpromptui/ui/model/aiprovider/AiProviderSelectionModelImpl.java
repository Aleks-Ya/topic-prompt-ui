package topicpromptui.ui.model.aiprovider;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import topicpromptui.core.ai.AiApi;
import topicpromptui.core.config.ConfigModel;
import topicpromptui.core.domain.AiProvider;
import topicpromptui.core.domain.AnswerType;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static topicpromptui.core.domain.AiProvider.GCP;
import static topicpromptui.core.domain.AiProvider.OPEN_AI;
import static topicpromptui.core.domain.AiProvider.OPEN_AI_GRAMMAR;
import static topicpromptui.core.domain.AiProvider.XAI;
import static topicpromptui.core.domain.AnswerType.AI_1;
import static topicpromptui.core.domain.AnswerType.AI_2;
import static topicpromptui.core.domain.AnswerType.AI_3;
import static topicpromptui.core.domain.AnswerType.GRAMMAR;

/**
 * Synchronized throughout: {@code QuestionModelImpl}'s executor threads read the selection while
 * building a request, so this follows the same invariant as {@code StateModelImpl}/{@code StorageModelImpl}.
 */
@Singleton
class AiProviderSelectionModelImpl implements AiProviderSelectionModel {
    private static final Logger log = LoggerFactory.getLogger(AiProviderSelectionModelImpl.class);
    private static final String KEY_PREFIX = "ai.provider.";

    private static final Map<AnswerType, AiProvider> DEFAULTS = Map.of(
            GRAMMAR, OPEN_AI_GRAMMAR,
            AI_1, OPEN_AI,
            AI_2, XAI,
            AI_3, GCP);

    private final Map<AnswerType, AiProvider> selection = new EnumMap<>(AnswerType.class);
    private final Set<AiProvider> boundProviders;

    @Inject
    AiProviderSelectionModelImpl(ConfigModel configModel, Map<AiProvider, AiApi> apis) {
        boundProviders = Set.copyOf(apis.keySet());
        for (var answerType : AnswerType.values()) {
            selection.put(answerType, readProvider(configModel, answerType));
        }
        log.info("AI provider per answer pane: {}", selection);
    }

    /** The property key for a slot is derived, so it can never drift out of sync with {@link AnswerType}. */
    static String propertyKey(AnswerType answerType) {
        return KEY_PREFIX + answerType.name().toLowerCase(Locale.ROOT);
    }

    /** A bad value in the config file must never keep the app from starting, so it warns and falls back. */
    private AiProvider readProvider(ConfigModel configModel, AnswerType answerType) {
        var fallback = DEFAULTS.get(answerType);
        var key = propertyKey(answerType);
        var value = configModel.getProperty(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        AiProvider provider;
        try {
            provider = AiProvider.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.warn("Unknown AI provider in '{}={}', falling back to {}. Known providers: {}",
                    key, value, fallback, Arrays.toString(AiProvider.values()));
            return fallback;
        }
        if (!boundProviders.contains(provider)) {
            log.warn("No AiApi bound for AI provider in '{}={}', falling back to {}. Bound providers: {}",
                    key, value, fallback, boundProviders);
            return fallback;
        }
        return provider;
    }

    @Override
    public synchronized AiProvider getProvider(AnswerType answerType) {
        return selection.get(answerType);
    }

    @Override
    public synchronized void setProvider(AnswerType answerType, AiProvider provider) {
        // Unlike a hand-edited config value, an unbound provider here can only be a programming error.
        if (!boundProviders.contains(provider)) {
            throw new IllegalArgumentException("No AiApi bound for " + provider + ", bound providers: " + boundProviders);
        }
        log.info("AI provider for {}: {} -> {}", answerType, selection.get(answerType), provider);
        selection.put(answerType, provider);
    }

    @Override
    public synchronized Map<AnswerType, AiProvider> getAll() {
        return Collections.unmodifiableMap(new EnumMap<>(selection));
    }
}

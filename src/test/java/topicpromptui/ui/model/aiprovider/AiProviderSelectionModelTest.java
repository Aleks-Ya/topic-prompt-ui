package topicpromptui.ui.model.aiprovider;

import org.junit.jupiter.api.Test;
import topicpromptui.core.ai.AiApi;
import topicpromptui.core.config.ConfigModel;
import topicpromptui.core.domain.AiProvider;
import topicpromptui.core.domain.AnswerType;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static topicpromptui.core.domain.AiProvider.CLAUDE;
import static topicpromptui.core.domain.AiProvider.GCP;
import static topicpromptui.core.domain.AiProvider.OPEN_AI;
import static topicpromptui.core.domain.AiProvider.OPEN_AI_GRAMMAR;
import static topicpromptui.core.domain.AiProvider.XAI;
import static topicpromptui.core.domain.AnswerType.AI_1;
import static topicpromptui.core.domain.AnswerType.AI_2;
import static topicpromptui.core.domain.AnswerType.AI_3;
import static topicpromptui.core.domain.AnswerType.GRAMMAR;

class AiProviderSelectionModelTest {
    private static final AiApi API = (systemPrompt, turns, onTextDelta) -> {
        throw new UnsupportedOperationException();
    };

    private static AiProviderSelectionModel model(Map<String, String> properties, AiProvider... boundProviders) {
        var apis = new EnumMap<AiProvider, AiApi>(AiProvider.class);
        for (var provider : boundProviders) {
            apis.put(provider, API);
        }
        return new AiProviderSelectionModelImpl(configWith(properties), apis);
    }

    private static AiProviderSelectionModel modelWithAllProvidersBound(Map<String, String> properties) {
        return model(properties, AiProvider.values());
    }

    private static ConfigModel configWith(Map<String, String> properties) {
        return new ConfigModel() {
            @Override
            public String getProperty(String propertyName) {
                return properties.get(propertyName);
            }

            @Override
            public Path getAppDataPath() {
                return null;
            }
        };
    }

    @Test
    void defaultsWhenConfigHasNoProviderProperties() {
        assertThat(modelWithAllProvidersBound(Map.of()).getAll())
                .containsExactlyInAnyOrderEntriesOf(Map.of(GRAMMAR, OPEN_AI_GRAMMAR, AI_1, OPEN_AI, AI_2, XAI, AI_3, GCP));
    }

    @Test
    void propertyKeysAreDerivedFromTheSlotNames() {
        assertThat(AnswerType.values()).extracting(AiProviderSelectionModelImpl::propertyKey)
                .containsExactly("ai.provider.grammar", "ai.provider.ai_1", "ai.provider.ai_2", "ai.provider.ai_3");
    }

    @Test
    void configOverridesOneSlotAndLeavesTheOthersAtTheirDefaults() {
        var model = modelWithAllProvidersBound(Map.of("ai.provider.ai_2", "CLAUDE"));
        assertThat(model.getProvider(AI_2)).isEqualTo(CLAUDE);
        assertThat(model.getProvider(AI_1)).isEqualTo(OPEN_AI);
        assertThat(model.getProvider(AI_3)).isEqualTo(GCP);
        assertThat(model.getProvider(GRAMMAR)).isEqualTo(OPEN_AI_GRAMMAR);
    }

    @Test
    void configOverridesEverySlotIncludingGrammar() {
        var model = modelWithAllProvidersBound(Map.of(
                "ai.provider.grammar", "XAI",
                "ai.provider.ai_1", "CLAUDE",
                "ai.provider.ai_2", "GCP",
                "ai.provider.ai_3", "OPEN_AI"));
        assertThat(model.getAll())
                .containsExactlyInAnyOrderEntriesOf(Map.of(GRAMMAR, XAI, AI_1, CLAUDE, AI_2, GCP, AI_3, OPEN_AI));
    }

    @Test
    void providerNameIsCaseInsensitiveAndTrimmed() {
        assertThat(modelWithAllProvidersBound(Map.of("ai.provider.ai_2", " Claude ")).getProvider(AI_2))
                .isEqualTo(CLAUDE);
    }

    @Test
    void unknownProviderNameFallsBackToTheDefault() {
        assertThat(modelWithAllProvidersBound(Map.of("ai.provider.ai_2", "GROK")).getProvider(AI_2)).isEqualTo(XAI);
    }

    @Test
    void blankProviderNameFallsBackToTheDefault() {
        assertThat(modelWithAllProvidersBound(Map.of("ai.provider.ai_2", "  ")).getProvider(AI_2)).isEqualTo(XAI);
    }

    @Test
    void providerWithoutAnAiApiBindingFallsBackToTheDefault() {
        var model = model(Map.of("ai.provider.ai_2", "CLAUDE"), OPEN_AI, OPEN_AI_GRAMMAR, GCP, XAI);
        assertThat(model.getProvider(AI_2)).isEqualTo(XAI);
    }

    @Test
    void setProviderOverridesTheConfiguredProvider() {
        var model = modelWithAllProvidersBound(Map.of("ai.provider.ai_2", "CLAUDE"));
        model.setProvider(AI_2, GCP);
        assertThat(model.getProvider(AI_2)).isEqualTo(GCP);
    }

    @Test
    void setProviderRejectsAProviderWithoutAnAiApiBinding() {
        var model = model(Map.of(), OPEN_AI, OPEN_AI_GRAMMAR, GCP, XAI);
        assertThatThrownBy(() -> model.setProvider(AI_2, CLAUDE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CLAUDE");
    }

    @Test
    void getAllIsAnImmutableSnapshot() {
        var model = modelWithAllProvidersBound(Map.of());
        var snapshot = model.getAll();
        model.setProvider(AI_2, CLAUDE);
        assertThat(snapshot).containsEntry(AI_2, XAI);
        assertThatThrownBy(() -> snapshot.put(AI_2, CLAUDE)).isInstanceOf(UnsupportedOperationException.class);
    }
}

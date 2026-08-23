package topicpromptui.ui.model;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import topicpromptui.core.ai.AiApi;
import topicpromptui.core.ai.AiModule;
import topicpromptui.core.domain.AiProvider;

/**
 * The complete provider table: every implemented {@link AiApi} keyed by {@link AiProvider},
 * including ones no answer pane currently uses. Entries link to the per-provider {@code @Named}
 * bindings rather than replacing them, so the {@code *ApiIT}s and {@code TestRootModule} keep
 * resolving providers by name (and test mocks bound over those names flow through this map too).
 * <p>
 * Lives in {@code ui.model} rather than in the {@code core.ai.*} provider modules because
 * {@code core.ai} deliberately does not depend on {@code core.domain}, where {@link AiProvider} lives.
 */
public class AiProviderModule extends AbstractModule {
    @Override
    protected void configure() {
        var apis = MapBinder.newMapBinder(binder(), AiProvider.class, AiApi.class);
        apis.addBinding(AiProvider.OPEN_AI).to(Key.get(AiApi.class, Names.named(AiModule.OPEN_AI)));
        apis.addBinding(AiProvider.OPEN_AI_GRAMMAR).to(Key.get(AiApi.class, Names.named(AiModule.OPEN_AI_GRAMMAR)));
        apis.addBinding(AiProvider.CLAUDE).to(Key.get(AiApi.class, Names.named(AiModule.CLAUDE_AI)));
        apis.addBinding(AiProvider.GCP).to(Key.get(AiApi.class, Names.named(AiModule.GCP_AI)));
        apis.addBinding(AiProvider.XAI).to(Key.get(AiApi.class, Names.named(AiModule.XAI)));
    }
}

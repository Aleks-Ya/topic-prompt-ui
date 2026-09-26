package topicpromptui.ui.model.aiprovider;

import com.google.inject.AbstractModule;

public class AiProviderSelectionModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AiProviderSelectionModel.class).to(AiProviderSelectionModelImpl.class);
    }
}

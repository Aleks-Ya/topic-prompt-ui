package topicpromptui.core.config;

import com.google.inject.AbstractModule;

public class ConfigurationModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ConfigModel.class).to(ConfigModelImpl.class);
    }
}

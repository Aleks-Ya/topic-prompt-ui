package topicpromptui.core.config;

import com.google.inject.AbstractModule;

/**
 * Drop-in replacement for {@link ConfigurationModule} in integration tests: keeps the real
 * credentials but isolates the app data path, so prompt templates come from the project rather than
 * from the developer's installed app. See {@link ProjectTemplatesConfigModel}.
 */
public class ProjectTemplatesConfigurationModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ConfigModel.class).to(ProjectTemplatesConfigModel.class);
    }
}

package topicpromptui.core.config;

import com.google.common.jimfs.Jimfs;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.nio.file.Path;

import static com.google.common.jimfs.Configuration.unix;

/**
 * Test {@link ConfigModel}: real API credentials from {@code ~/.topic-prompt-ui/config.properties},
 * but an empty in-memory app data path.
 * <p>
 * {@code PromptFactoryImpl} materializes its Freemarker templates into
 * {@code <appDataPath>/templates} once and reads them from there afterwards, so a test using the
 * production app data path would render whatever the developer's installed app has in
 * {@code ~/.topic-prompt-ui/templates} — a copy that is only written on first use and therefore
 * silently stales out whenever a template changes in the repository. Pointing the app data path at
 * a fresh in-memory filesystem forces the copy to happen every run, from the project's own
 * {@code src/main/resources/topicpromptui/core/prompt/*.ftl} on the test classpath.
 */
@Singleton
class ProjectTemplatesConfigModel implements ConfigModel {
    private final ConfigModel delegate;
    private final Path appDataPath;

    @Inject
    ProjectTemplatesConfigModel(ConfigModelImpl delegate) {
        this.delegate = delegate;
        appDataPath = Jimfs.newFileSystem(unix()).getPath("/app-data");
    }

    @Override
    public String getProperty(String propertyName) {
        return delegate.getProperty(propertyName);
    }

    @Override
    public Path getAppDataPath() {
        return appDataPath;
    }
}

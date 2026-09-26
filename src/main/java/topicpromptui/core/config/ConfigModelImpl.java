package topicpromptui.core.config;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Singleton
class ConfigModelImpl implements ConfigModel, ConfigWriter {
    private static final Logger log = LoggerFactory.getLogger(ConfigModelImpl.class);
    private final Properties properties = new Properties();
    private final Path appDataPath;
    private final Path configPath;

    @Inject
    public ConfigModelImpl(FileSystem fileSystem) {
        try {
            appDataPath = fileSystem.getPath(System.getProperty("user.home"), ".topic-prompt-ui");
            configPath = appDataPath.resolve("config.properties");
            if (Files.exists(configPath)) {
                try (var inputStream = Files.newInputStream(configPath)) {
                    properties.load(inputStream);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }

    @Override
    public Path getAppDataPath() {
        return appDataPath;
    }

    /**
     * Rewrites the whole file, so keys this app never reads are preserved but comments and key order
     * are not ({@code Properties.store} also prepends a timestamp line).
     */
    @Override
    public synchronized void setProperty(String propertyName, String value) {
        properties.setProperty(propertyName, value);
        try {
            Files.createDirectories(appDataPath);
            try (var outputStream = Files.newOutputStream(configPath)) {
                properties.store(outputStream, "Topic Prompt UI configuration");
            }
        } catch (IOException e) {
            log.warn("Failed to persist {} to {}, the value applies for this session only", propertyName, configPath, e);
        }
    }
}

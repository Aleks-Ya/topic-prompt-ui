package gptui.model.config;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Singleton
class ConfigModelImpl implements ConfigModel {
    private final Properties properties = new Properties();
    private final Path appDataPath;

    @Inject
    public ConfigModelImpl(FileSystem fileSystem) {
        try {
            appDataPath = fileSystem.getPath(System.getProperty("user.home"), ".gpt");
            var configPath = appDataPath.resolve("config.properties");
            if (Files.exists(configPath)) {
                properties.load(Files.newInputStream(configPath));
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
}

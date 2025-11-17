package gptui.model.config;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.Properties;

@Singleton
class ConfigModelImpl implements ConfigModel {
    private final Properties properties = new Properties();

    @Inject
    public ConfigModelImpl(FileSystem fileSystem) {
        try {
            var configPath = fileSystem.getPath(System.getProperty("user.home"), ".gpt", "config.properties");
            properties.load(Files.newInputStream(configPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }
}

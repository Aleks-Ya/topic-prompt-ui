package gptui.core.config;

import java.nio.file.Path;

public interface ConfigModel {
    String getProperty(String propertyName);

    Path getAppDataPath();
}

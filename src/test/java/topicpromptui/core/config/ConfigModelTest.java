package topicpromptui.core.config;

import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.jimfs.Configuration.unix;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ConfigModelTest {
    private final FileSystem fileSystem = Jimfs.newFileSystem(unix());

    private Path appDataPath() {
        return fileSystem.getPath(System.getProperty("user.home"), ".topic-prompt-ui");
    }

    private ConfigModelImpl configModel() {
        return new ConfigModelImpl(fileSystem);
    }

    @Test
    void writesAPropertyWhenNeitherDirectoryNorFileExists() {
        var model = configModel();
        model.setProperty("ai.provider.ai_2", "CLAUDE");
        assertThat(model.getProperty("ai.provider.ai_2")).isEqualTo("CLAUDE");
        assertThat(appDataPath().resolve("config.properties")).exists();
    }

    @Test
    void aFreshInstanceReadsBackWhatWasWritten() {
        configModel().setProperty("ai.provider.ai_2", "CLAUDE");
        assertThat(configModel().getProperty("ai.provider.ai_2")).isEqualTo("CLAUDE");
    }

    @Test
    void propertiesThisAppNeverReadsSurviveAWrite() throws IOException {
        Files.createDirectories(appDataPath());
        Files.writeString(appDataPath().resolve("config.properties"), "openai.token=secret\nunrelated.key=kept\n");

        configModel().setProperty("ai.provider.ai_2", "CLAUDE");

        var reloaded = configModel();
        assertThat(reloaded.getProperty("openai.token")).isEqualTo("secret");
        assertThat(reloaded.getProperty("unrelated.key")).isEqualTo("kept");
        assertThat(reloaded.getProperty("ai.provider.ai_2")).isEqualTo("CLAUDE");
    }

    /** A config directory the app cannot write must cost durability, not the running session. */
    @Test
    void aFailedWriteIsSwallowedAndTheValueStillApplies() throws IOException {
        Files.createDirectories(appDataPath().getParent());
        Files.writeString(appDataPath(), "not a directory");
        var model = configModel();

        assertThatCode(() -> model.setProperty("ai.provider.ai_2", "CLAUDE")).doesNotThrowAnyException();
        assertThat(model.getProperty("ai.provider.ai_2")).isEqualTo("CLAUDE");
    }
}

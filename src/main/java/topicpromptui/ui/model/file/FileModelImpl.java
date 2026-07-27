package topicpromptui.ui.model.file;

import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static topicpromptui.core.util.ResourceUtils.resourceIS;
import static topicpromptui.core.util.ResourceUtils.resourceUrl;
import static java.util.Objects.requireNonNull;

@Singleton
class FileModelImpl implements FileModel {
    private static final Logger log = LoggerFactory.getLogger(FileModelImpl.class);
    // Desktop.open(...) can block for a long time (or hang outright, e.g. no mime handler
    // configured / a nested JavaFX modal dialog fighting AWT's toolkit init) - run it off the FX
    // Application Thread so a stuck OS call can't freeze the UI. Daemon: nothing ever shuts this
    // down and a hung open() must not keep the JVM alive after the window closes.
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(runnable -> {
        var thread = new Thread(runnable);
        thread.setDaemon(true);
        return thread;
    });

    @Override
    public InputStream getAppIcon() {
        return resourceIS(getClass(), "icon.png");
    }

    @Override
    public String getAppVersion() {
        log.info("Reading application version...");
        try {
            var is = resourceIS(getClass(), "/topicpromptui/version.txt");
            try (var dataInputStream = new DataInputStream(is)) {
                var bytes = new byte[is.available()];
                dataInputStream.readFully(bytes);
                var version = new String(bytes, StandardCharsets.UTF_8);
                log.info("Application version: {}", version);
                return version;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public URL getFxmlLocation() {
        log.info("Java version: {}", Runtime.version());
        var topicPromptUiFxml = resourceUrl(getClass(), "/topicpromptui/ui/view/TopicPromptUi.fxml");
        log.info("TopicPromptUi.fxml: {}", topicPromptUiFxml);
        return requireNonNull(topicPromptUiFxml);
    }

    @Override
    public void openFile(Path path) {
        log.info("Opening file: {}", path);
        EXECUTOR.execute(() -> {
            try {
                Desktop.getDesktop().open(path.toFile());
            } catch (IOException e) {
                log.error("Failed to open file: {}", path, e);
            }
        });
    }
}

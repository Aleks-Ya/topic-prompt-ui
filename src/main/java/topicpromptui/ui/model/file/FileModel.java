package topicpromptui.ui.model.file;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

public interface FileModel {
    InputStream getAppIcon();

    String getAppVersion();

    URL getFxmlLocation();

    void openFile(Path path);
}

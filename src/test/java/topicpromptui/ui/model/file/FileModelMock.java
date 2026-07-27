package topicpromptui.ui.model.file;

import jakarta.inject.Singleton;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class FileModelMock implements FileModel {
    private final FileModel delegate = new FileModelImpl();
    private final List<Path> openedFiles = new ArrayList<>();

    @Override
    public InputStream getAppIcon() {
        return delegate.getAppIcon();
    }

    @Override
    public String getAppVersion() {
        return delegate.getAppVersion();
    }

    @Override
    public URL getFxmlLocation() {
        return delegate.getFxmlLocation();
    }

    @Override
    public synchronized void openFile(Path path) {
        openedFiles.add(path);
    }

    public synchronized List<Path> getOpenedFiles() {
        return List.copyOf(openedFiles);
    }
}

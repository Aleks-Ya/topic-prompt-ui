package topicpromptui.core.storagefilesystem;

import com.google.inject.AbstractModule;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

public class StorageFilesystemModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(FileSystem.class).toInstance(FileSystems.getDefault());
        bind(StorageFilesystem.class).to(StorageFilesystemImpl.class);
    }
}

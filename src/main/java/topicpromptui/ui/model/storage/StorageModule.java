package topicpromptui.ui.model.storage;

import com.google.inject.AbstractModule;
import topicpromptui.core.storagefilesystem.StorageFilesystemModule;

public class StorageModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new StorageFilesystemModule());
        bind(StorageModel.class).to(StorageModelImpl.class);
    }
}

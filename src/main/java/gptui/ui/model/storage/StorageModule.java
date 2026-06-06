package gptui.ui.model.storage;

import com.google.inject.AbstractModule;
import gptui.core.storagefilesystem.StorageFilesystemModule;

public class StorageModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new StorageFilesystemModule());
        bind(StorageModel.class).to(StorageModelImpl.class);
    }
}

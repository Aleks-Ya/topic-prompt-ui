package gptui.core.storagefilesystem;

import java.util.List;

public interface StorageFilesystem {
    void saveInteraction(Interaction interaction);

    List<Interaction> readAllInteractions();

    void deleteInteraction(InteractionId interactionId);

    List<Theme> readThemes();

    void saveThemes(List<Theme> themes);
}

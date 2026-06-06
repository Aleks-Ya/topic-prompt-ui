package gptui.ui.model.storage;

import gptui.core.storagefilesystem.Interaction;
import gptui.core.storagefilesystem.InteractionId;
import gptui.core.storagefilesystem.Theme;
import gptui.core.storagefilesystem.ThemeId;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface StorageModel {
    InteractionId newInteractionId();

    void updateInteraction(InteractionId interactionId, Function<Interaction, Interaction> update);

    void saveInteraction(Interaction interaction);

    Optional<Interaction> readInteraction(InteractionId interactionId);

    List<Interaction> readAllInteractions();

    void deleteInteraction(InteractionId interactionId);

    List<Theme> getThemes();

    Theme addTheme(String theme);

    void saveTheme(Theme theme);

    Theme getTheme(ThemeId themeId);
}

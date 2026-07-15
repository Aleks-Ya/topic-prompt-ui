package topicpromptui.core.storagefilesystem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import topicpromptui.core.config.ConfigModel;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// The "public" modifier is required: StorageModelTest lives in a different package
// (topicpromptui.ui.model.storage) and constructs this class directly.
@Singleton
public class StorageFilesystemImpl implements StorageFilesystem {
    private static final Logger log = LoggerFactory.getLogger(StorageFilesystemImpl.class);
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(InteractionId.class, new InteractionIdSerDe())
            .registerTypeAdapter(TopicId.class, new TopicIdSerDe())
            .setPrettyPrinting()
            .create();
    private final Path interactionsDir;
    private final Path topicsFile;

    @Inject
    public StorageFilesystemImpl(ConfigModel config) {
        try {
            var storageDir = config.getAppDataPath().resolve("storage");
            log.info("Storage directory: {}", storageDir);
            if (Files.notExists(storageDir)) {
                Files.createDirectories(storageDir);
            }
            interactionsDir = storageDir.resolve("interactions");
            if (Files.notExists(interactionsDir)) {
                Files.createDirectories(interactionsDir);
            }
            topicsFile = storageDir.resolve("topics.json");
            if (Files.notExists(topicsFile)) {
                saveTopics(List.of());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void saveInteraction(Interaction interaction) {
        try {
            var file = getInteractionFile(interaction.id());
            var json = gson.toJson(interaction);
            Files.writeString(file, json);
            log.info("Interaction was saved to file: {}", file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path getInteractionFile(InteractionId interactionId) {
        return interactionsDir.resolve(interactionId.id() + ".json");
    }

    @Override
    public synchronized List<Interaction> readAllInteractions() {
        try {
            var result = new ArrayList<Interaction>();
            try (var files = Files.list(interactionsDir)) {
                for (var file : files.toList()) {
                    var interaction = gson.fromJson(Files.readString(file), Interaction.class);
                    result.add(interaction);
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void deleteInteraction(InteractionId interactionId) {
        try {
            Files.delete(getInteractionFile(interactionId));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized List<Topic> readTopics() {
        try {
            var json = Files.readString(topicsFile);
            var type = new TypeToken<List<Topic>>() {
            }.getType();
            List<Topic> topics = gson.fromJson(json, type);
            log.trace("Topics were read from file ({} total): {}", topics.size(), topics);
            return topics;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void saveTopics(List<Topic> topics) {
        try {
            var json = gson.toJson(topics);
            Files.writeString(topicsFile, json);
            log.info("Topics were saved to file ({} total): {}", topics.size(), topics);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

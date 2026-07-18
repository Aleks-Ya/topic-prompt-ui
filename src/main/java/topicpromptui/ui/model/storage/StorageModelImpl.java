package topicpromptui.ui.model.storage;

import topicpromptui.core.storagefilesystem.Interaction;
import topicpromptui.core.storagefilesystem.InteractionId;
import topicpromptui.core.storagefilesystem.StorageFilesystem;
import topicpromptui.core.storagefilesystem.Topic;
import topicpromptui.core.storagefilesystem.TopicId;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Singleton
class StorageModelImpl implements StorageModel {
    private static final Logger log = LoggerFactory.getLogger(StorageModelImpl.class);
    private final Map<InteractionId, Interaction> interactions = new LinkedHashMap<>();
    private final List<Topic> topicList = new ArrayList<>();
    private final StorageFilesystem storageFilesystem;
    private final Map<TopicId, Topic> topicMap = new HashMap<>();
    private long lastIssuedId;

    @Inject
    public StorageModelImpl(StorageFilesystem storageFilesystem) {
        this.storageFilesystem = storageFilesystem;
        storageFilesystem.readAllInteractions().forEach(interaction -> interactions.put(interaction.id(), interaction));
        lastIssuedId = interactions.keySet().stream().mapToLong(InteractionId::id).max().orElse(0L);
        readTopicsFromInteractions();
    }

    private void readTopicsFromInteractions() {
        topicMap.clear();
        storageFilesystem.readTopics().stream()
                .filter(topic -> !topicList.contains(topic))
                .forEach(topicList::add);
        storageFilesystem.readTopics().forEach(topic -> topicMap.put(topic.id(), topic));
        var sortedTopicIds = readAllInteractions().stream().map(Interaction::topicId).distinct().toList();
        sortedTopicIds.forEach(topicId -> {
            var topic = topicMap.get(topicId);
            topicList.remove(topic);
            topicList.addLast(topic);
        });
    }

    @Override
    public synchronized InteractionId newInteractionId() {
        // Epoch seconds alone collide when two interactions are created within the same second,
        // silently merging them (same in-memory key and same <id>.json file).
        lastIssuedId = Math.max(Instant.now().getEpochSecond(), lastIssuedId + 1);
        var interactionId = new InteractionId(lastIssuedId);
        log.trace("newInteractionId: {}", interactionId);
        return interactionId;
    }

    @Override
    public synchronized void updateInteraction(InteractionId interactionId, UnaryOperator<Interaction> update) {
        var interactionOpt = readInteraction(interactionId);
        if (interactionOpt.isEmpty()) {
            // A streaming answer may finish after the user deleted its interaction;
            // dropping the late update here keeps the deletion final.
            log.warn("Skip updating nonexistent interaction: {}", interactionId);
            return;
        }
        saveInteraction(update.apply(interactionOpt.get()));
    }

    @Override
    public synchronized void saveInteraction(Interaction interaction) {
        interactions.put(interaction.id(), interaction);
        lastIssuedId = Math.max(lastIssuedId, interaction.id().id());
        var topic = getTopic(interaction.topicId());
        topicList.remove(topic);
        topicList.addFirst(topic);
        storageFilesystem.saveInteraction(interaction);
    }

    @Override
    public synchronized Optional<Interaction> readInteraction(InteractionId interactionId) {
        if (interactionId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(interactions.get(interactionId));
    }

    @Override
    public synchronized List<Interaction> readAllInteractions() {
        return interactions.values().stream()
                .sorted((i1, i2) -> i2.id().id().compareTo(i1.id().id()))
                .toList();
    }

    @Override
    public synchronized void deleteInteraction(InteractionId interactionId) {
        storageFilesystem.deleteInteraction(interactionId);
        interactions.remove(interactionId);
    }

    @Override
    public synchronized List<Topic> getTopics() {
        // Copy: saveInteraction reorders topicList on the AI executor threads, so handing out
        // the live list would let the FX thread iterate it concurrently with that mutation.
        return List.copyOf(topicList);
    }

    @Override
    public synchronized Topic addTopic(String topic) {
        log.trace("Adding topic: {}", topic);
        var trimmed = topic.trim();
        var existingTopics = storageFilesystem.readTopics();
        var existingOpt = existingTopics.stream().filter(topicObj -> topicObj.title().equals(trimmed)).findFirst();
        var newTopicExists = existingOpt.isPresent();
        if (!newTopicExists) {
            var topics = new ArrayList<>(existingTopics);
            var maxId = existingTopics.stream().map(Topic::id).mapToLong(TopicId::id).max().orElse(0L);
            var newId = ++maxId;
            var newTopic = new Topic(new TopicId(newId), topic);
            topics.add(newTopic);
            storageFilesystem.saveTopics(topics);
            updateTopicCaches(topics);
            log.trace("Topic was added: {}", newTopic);
            return newTopic;
        } else {
            log.trace("Skip adding existing Topic: {}", existingOpt.get());
            return existingOpt.get();
        }
    }

    @Override
    public synchronized Topic renameTopic(TopicId topicId, String newTitle) {
        log.trace("Renaming topic {} to '{}'", topicId, newTitle);
        var trimmed = newTitle.trim();
        var currentTopic = getTopic(topicId);
        if (trimmed.equals(currentTopic.title())) {
            log.trace("New title equals current title, no-op: {}", currentTopic);
            return currentTopic;
        }
        var existingTopics = storageFilesystem.readTopics();
        var targetOpt = existingTopics.stream()
                .filter(topic -> topic.title().equals(trimmed) && !topic.id().equals(topicId))
                .findFirst();
        if (targetOpt.isEmpty()) {
            var renamedTopic = new Topic(topicId, trimmed);
            var topics = existingTopics.stream()
                    .map(topic -> topic.id().equals(topicId) ? renamedTopic : topic)
                    .toList();
            storageFilesystem.saveTopics(topics);
            updateTopicCaches(topics);
            log.trace("Topic was renamed: {}", renamedTopic);
            return renamedTopic;
        } else {
            var targetTopic = targetOpt.get();
            log.trace("Merging topic {} into existing topic {}", currentTopic, targetTopic);
            readAllInteractions().stream()
                    .filter(interaction -> interaction.topicId().equals(topicId))
                    .forEach(interaction -> updateInteraction(interaction.id(), i -> i.withTopicId(targetTopic.id())));
            var topics = existingTopics.stream()
                    .filter(topic -> !topic.id().equals(topicId))
                    .toList();
            storageFilesystem.saveTopics(topics);
            updateTopicCaches(topics);
            log.trace("Topic was merged and removed: {}", currentTopic);
            return targetTopic;
        }
    }

    @Override
    public synchronized void deleteTopic(TopicId topicId) {
        var currentTopic = getTopic(topicId);
        log.trace("Deleting topic: {}", currentTopic);
        readAllInteractions().stream()
                .filter(interaction -> interaction.topicId().equals(topicId))
                .forEach(interaction -> deleteInteraction(interaction.id()));
        var topics = storageFilesystem.readTopics().stream()
                .filter(topic -> !topic.id().equals(topicId))
                .toList();
        storageFilesystem.saveTopics(topics);
        updateTopicCaches(topics);
        log.trace("Topic was deleted: {}", currentTopic);
    }

    private void updateTopicCaches(List<Topic> topics) {
        topicList.clear();
        topicList.addAll(topics);
        topicMap.clear();
        topicMap.putAll(topics.stream().collect(toMap(Topic::id, identity())));
    }

    @Override
    public synchronized void saveTopic(Topic topic) {
        var existingTopics = storageFilesystem.readTopics();
        var existingOpt = existingTopics.stream().filter(topicObj -> topicObj.id().equals(topic.id())).findFirst();
        var newTopicExists = existingOpt.isPresent();
        if (!newTopicExists) {
            var topics = new ArrayList<>(existingTopics);
            topics.add(topic);
            storageFilesystem.saveTopics(topics);
            updateTopicCaches(topics);
        }
    }

    @Override
    public synchronized Topic getTopic(TopicId topicId) {
        var topic = topicMap.get(topicId);
        if (topic == null) {
            throw new IllegalStateException("Topic was not found by id: " + topicId);
        }
        return topic;
    }
}

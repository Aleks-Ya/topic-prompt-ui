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

    @Inject
    public StorageModelImpl(StorageFilesystem storageFilesystem) {
        this.storageFilesystem = storageFilesystem;
        storageFilesystem.readAllInteractions().forEach(interaction -> interactions.put(interaction.id(), interaction));
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
        var interactionId = new InteractionId(Instant.now().getEpochSecond());
        log.trace("newInteractionId: {}", interactionId);
        return interactionId;
    }

    @Override
    public synchronized void updateInteraction(InteractionId interactionId, UnaryOperator<Interaction> update) {
        var interactionOpt = readInteraction(interactionId);
        Interaction interaction;
        if (interactionOpt.isEmpty()) {
            interaction = new Interaction(interactionId, null, null, null, null, null);
            if (interactions.containsKey(interactionId)) {
                throw new IllegalStateException("Interaction already exists: " + interaction);
            }
        } else {
            interaction = interactionOpt.get();
        }
        var updatedInteraction = update.apply(interaction);
        saveInteraction(updatedInteraction);
    }

    @Override
    public synchronized void saveInteraction(Interaction interaction) {
        interactions.put(interaction.id(), interaction);
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
    public List<Topic> getTopics() {
        return topicList;
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

    private void updateTopicCaches(List<Topic> topics) {
        topicList.clear();
        topicList.addAll(topics);
        topicMap.clear();
        topicMap.putAll(topics.stream().collect(toMap(Topic::id, identity())));
    }

    @Override
    public void saveTopic(Topic topic) {
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
    public Topic getTopic(TopicId topicId) {
        var topic = topicMap.get(topicId);
        if (topic == null) {
            throw new IllegalStateException("Topic was not found by id: " + topicId);
        }
        return topic;
    }
}

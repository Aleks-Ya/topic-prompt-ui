package topicpromptui.ui.model.state;

import topicpromptui.core.storagefilesystem.Answer;
import topicpromptui.core.storagefilesystem.Interaction;
import topicpromptui.core.storagefilesystem.InteractionId;
import topicpromptui.core.storagefilesystem.InteractionType;
import topicpromptui.ui.model.storage.StorageModel;
import topicpromptui.core.storagefilesystem.Topic;
import topicpromptui.core.storagefilesystem.TopicId;
import topicpromptui.core.util.Mdc;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static topicpromptui.core.storagefilesystem.AnswerState.NEW;
import static topicpromptui.core.storagefilesystem.AnswerType.CLAUDE;
import static topicpromptui.core.storagefilesystem.AnswerType.GCP;
import static topicpromptui.core.storagefilesystem.AnswerType.GRAMMAR;
import static topicpromptui.core.storagefilesystem.AnswerType.OPEN_AI;

@Singleton
class StateModelImpl implements StateModel {
    private static final Logger log = LoggerFactory.getLogger(StateModelImpl.class);
    private final StorageModel storage;
    private InteractionId currentInteractionId;
    private Topic currentTopic;
    private String editedQuestion;
    private Boolean isHistoryFilteringEnabled = false;
    private String historyFilterText = "";

    @Inject
    StateModelImpl(StorageModel storage) {
        this.storage = storage;
    }

    @Override
    public synchronized boolean isEnteringNewQuestion() {
        return getCurrentInteractionOpt()
                .map(interaction -> !Objects.equals(interaction.question(), getEditedQuestion()))
                .orElse(false);
    }

    @Override
    public synchronized List<Interaction> getFullHistory() {
        return storage.readAllInteractions();
    }

    @Override
    public synchronized List<Interaction> getFilteredHistory() {
        var historyFilteringEnabled = isHistoryFilteringEnabled();
        var filterText = getHistoryFilterText();
        var filterTextLower = filterText.toLowerCase();
        return getFullHistory().stream()
                .filter(interaction -> !historyFilteringEnabled ||
                        Objects.equals(getCurrentTopic(), storage.getTopic(interaction.topicId())))
                .filter(interaction -> filterText.isBlank() ||
                        interaction.question().toLowerCase().contains(filterTextLower))
                .toList();
    }

    @Override
    public synchronized InteractionId getCurrentInteractionId() {
        log.trace("getCurrentInteractionId: '{}'", currentInteractionId);
        return currentInteractionId;
    }

    @Override
    public synchronized Optional<Interaction> getCurrentInteractionOpt() {
        var interaction = storage.readInteraction(currentInteractionId);
        log.trace("getCurrentInteractionOpt: '{}'", interaction.map(Interaction::toShortString));
        return interaction;
    }

    @Override
    public synchronized void setCurrentInteractionId(InteractionId currentInteractionId) {
        log.trace("setCurrentInteractionId: {}", currentInteractionId);
        this.currentInteractionId = currentInteractionId;
    }

    @Override
    public synchronized InteractionId createInteraction(InteractionType interactionType, InteractionId parentInteractionId) {
        var interactionId = storage.newInteractionId();
        Mdc.run(interactionId.id(), () -> {
            var topic = getCurrentTopic();
            var question = getEditedQuestion();
            var interaction = new Interaction(interactionId, interactionType, topic.id(), question, Map.of(
                    GRAMMAR, new Answer(GRAMMAR, "", "", "", NEW, null, null, null, null, null, null, null),
                    OPEN_AI, new Answer(OPEN_AI, "", "", "", NEW, null, null, null, null, null, null, null),
                    CLAUDE, new Answer(CLAUDE, "", "", "", NEW, null, null, null, null, null, null, null),
                    GCP, new Answer(GCP, "", "", "", NEW, null, null, null, null, null, null, null)
            ), parentInteractionId);
            storage.saveInteraction(interaction);
            setCurrentInteractionId(interactionId);
        });
        return interactionId;
    }

    @Override
    public synchronized void deleteCurrentInteraction() {
        var currentInteractionOpt = getCurrentInteractionOpt();
        if (currentInteractionOpt.isEmpty()) {
            return;
        }
        var currentInteraction = currentInteractionOpt.get();
        var newCurrentInteraction = pickNextCurrentInteractionAfterDeletion(currentInteraction);
        storage.deleteInteraction(currentInteraction.id());
        setCurrentInteractionId(newCurrentInteraction != null ? newCurrentInteraction.id() : null);
    }

    private Interaction pickNextCurrentInteractionAfterDeletion(Interaction currentInteraction) {
        var history = getFilteredHistory();
        if (history.size() > 1) {
            var neighborInteraction = adjacentItem(history, history.indexOf(currentInteraction));
            setCurrentTopic(storage.getTopic(neighborInteraction.topicId()));
            return neighborInteraction;
        }
        return switchToAdjacentTopicAndPickFirstInteraction();
    }

    private Interaction switchToAdjacentTopicAndPickFirstInteraction() {
        var oldCurrentTopic = getCurrentTopic();
        var topics = getTopics();
        if (oldCurrentTopic == null || topics.size() <= 1) {
            return null;
        }
        setCurrentTopic(adjacentItem(topics, topics.indexOf(oldCurrentTopic)));
        var newHistory = getFilteredHistory();
        return newHistory.isEmpty() ? null : newHistory.getFirst();
    }

    private static <T> T adjacentItem(List<T> list, int index) {
        return list.get(index == 0 ? index + 1 : index - 1);
    }

    @Override
    public synchronized List<Topic> getTopics() {
        return storage.getTopics();
    }

    @Override
    public synchronized Topic addTopic(String topic) {
        return storage.addTopic(topic);
    }

    @Override
    public synchronized Topic renameTopic(TopicId topicId, String newTitle) {
        return storage.renameTopic(topicId, newTitle);
    }

    @Override
    public synchronized void deleteTopic(TopicId topicId) {
        var topics = getTopics();
        var adjacentTopic = topics.size() > 1 ? adjacentItem(topics, topics.indexOf(getTopic(topicId))) : null;
        storage.deleteTopic(topicId);
        setCurrentTopic(adjacentTopic);
        if (getCurrentInteractionOpt().isEmpty()) {
            chooseFirstInteractionAsCurrent();
            getCurrentInteractionOpt().ifPresent(interaction -> setCurrentTopic(storage.getTopic(interaction.topicId())));
        }
    }

    @Override
    public synchronized Topic getTopic(TopicId topicId) {
        return storage.getTopic(topicId);
    }

    @Override
    public synchronized Long getInteractionCountInTopic(String topic) {
        return getFullHistory().stream()
                .filter(interaction -> Objects.equals(storage.getTopic(interaction.topicId()).title(), topic))
                .count();
    }

    @Override
    public synchronized Topic getCurrentTopic() {
        return currentTopic;
    }

    @Override
    public synchronized void setCurrentTopic(Topic currentTopic) {
        log.trace("setCurrentTopic: '{}'", currentTopic);
        this.currentTopic = currentTopic;
    }

    @Override
    public synchronized void setFirstTopicAsCurrent() {
        setCurrentTopic(!getTopics().isEmpty() ? getTopics().getFirst() : null);
    }

    @Override
    public synchronized String getEditedQuestion() {
        return editedQuestion;
    }

    @Override
    public synchronized void setEditedQuestion(String question) {
        log.trace("setEditedQuestion: '{}'", question);
        this.editedQuestion = question;
    }

    @Override
    public synchronized Boolean isHistoryFilteringEnabled() {
        log.trace("isHistoryFilteringEnabled: {}", isHistoryFilteringEnabled);
        return isHistoryFilteringEnabled;
    }

    @Override
    public synchronized void setIsHistoryFilteringEnabled(Boolean isHistoryFilteringEnabled) {
        log.trace("setIsHistoryFilteringEnabled: {}", isHistoryFilteringEnabled);
        this.isHistoryFilteringEnabled = isHistoryFilteringEnabled;
    }

    @Override
    public synchronized String getHistoryFilterText() {
        log.trace("getHistoryFilterText: '{}'", historyFilterText);
        return historyFilterText;
    }

    @Override
    public synchronized void setHistoryFilterText(String historyFilterText) {
        log.trace("setHistoryFilterText: '{}'", historyFilterText);
        this.historyFilterText = historyFilterText != null ? historyFilterText : "";
    }

    @Override
    public synchronized void chooseFirstInteractionAsCurrent() {
        if (!getFilteredHistory().isEmpty()) {
            setCurrentInteractionId(getFilteredHistory().getFirst().id());
        } else {
            setCurrentInteractionId(null);
        }
    }
}

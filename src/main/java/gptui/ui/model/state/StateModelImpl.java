package gptui.ui.model.state;

import gptui.core.storagefilesystem.Answer;
import gptui.core.storagefilesystem.Interaction;
import gptui.core.storagefilesystem.InteractionId;
import gptui.core.storagefilesystem.InteractionType;
import gptui.ui.model.storage.StorageModel;
import gptui.core.storagefilesystem.Theme;
import gptui.core.storagefilesystem.ThemeId;
import gptui.core.util.Mdc;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static gptui.core.storagefilesystem.AnswerState.NEW;
import static gptui.core.storagefilesystem.AnswerType.CLAUDE;
import static gptui.core.storagefilesystem.AnswerType.GCP;
import static gptui.core.storagefilesystem.AnswerType.GRAMMAR;
import static gptui.core.storagefilesystem.AnswerType.OPEN_AI;

@Singleton
class StateModelImpl implements StateModel {
    private static final Logger log = LoggerFactory.getLogger(StateModelImpl.class);
    private final StorageModel storage;
    private InteractionId currentInteractionId;
    private Theme currentTheme;
    private String editedQuestion;
    private Boolean isHistoryFilteringEnabled = false;

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
        return getFullHistory().stream()
                .filter(interaction -> !historyFilteringEnabled ||
                        Objects.equals(getCurrentTheme(), storage.getTheme(interaction.themeId())))
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
    public InteractionId createInteraction(InteractionType interactionType, InteractionId parentInteractionId) {
        var interactionId = storage.newInteractionId();
        Mdc.run(interactionId.id(), () -> {
            var theme = getCurrentTheme();
            var question = getEditedQuestion();
            var interaction = new Interaction(interactionId, interactionType, theme.id(), question, Map.of(
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
            setCurrentTheme(storage.getTheme(neighborInteraction.themeId()));
            return neighborInteraction;
        }
        return switchToAdjacentThemeAndPickFirstInteraction();
    }

    private Interaction switchToAdjacentThemeAndPickFirstInteraction() {
        var oldCurrentTheme = getCurrentTheme();
        var themes = getThemes();
        if (oldCurrentTheme == null || themes.size() <= 1) {
            return null;
        }
        setCurrentTheme(adjacentItem(themes, themes.indexOf(oldCurrentTheme)));
        var newHistory = getFilteredHistory();
        return newHistory.isEmpty() ? null : newHistory.getFirst();
    }

    private static <T> T adjacentItem(List<T> list, int index) {
        return list.get(index == 0 ? index + 1 : index - 1);
    }

    @Override
    public synchronized List<Theme> getThemes() {
        return storage.getThemes();
    }

    @Override
    public Theme addTheme(String theme) {
        return storage.addTheme(theme);
    }

    @Override
    public Theme renameTheme(ThemeId themeId, String newTitle) {
        return storage.renameTheme(themeId, newTitle);
    }

    @Override
    public Theme getTheme(ThemeId themeId) {
        return storage.getTheme(themeId);
    }

    @Override
    public Long getInteractionCountInTheme(String theme) {
        return getFullHistory().stream()
                .filter(interaction -> Objects.equals(storage.getTheme(interaction.themeId()).title(), theme))
                .count();
    }

    @Override
    public synchronized Theme getCurrentTheme() {
        return currentTheme;
    }

    @Override
    public synchronized void setCurrentTheme(Theme currentTheme) {
        log.trace("setCurrentTheme: '{}'", currentTheme);
        this.currentTheme = currentTheme;
    }

    @Override
    public synchronized void setFirstThemeAsCurrent() {
        setCurrentTheme(!getThemes().isEmpty() ? getThemes().getFirst() : null);
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
    public Boolean isHistoryFilteringEnabled() {
        log.trace("isHistoryFilteringEnabled: {}", isHistoryFilteringEnabled);
        return isHistoryFilteringEnabled;
    }

    @Override
    public void setIsHistoryFilteringEnabled(Boolean isHistoryFilteringEnabled) {
        log.trace("setIsHistoryFilteringEnabled: {}", isHistoryFilteringEnabled);
        this.isHistoryFilteringEnabled = isHistoryFilteringEnabled;
    }

    @Override
    public void chooseFirstInteractionAsCurrent() {
        if (!getFilteredHistory().isEmpty()) {
            setCurrentInteractionId(getFilteredHistory().getFirst().id());
        } else {
            setCurrentInteractionId(null);
        }
    }
}

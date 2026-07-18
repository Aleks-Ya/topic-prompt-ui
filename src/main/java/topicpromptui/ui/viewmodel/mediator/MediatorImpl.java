package topicpromptui.ui.viewmodel.mediator;

import topicpromptui.ui.model.clipboard.ClipboardModel;
import topicpromptui.ui.model.file.FileModel;
import topicpromptui.ui.model.question.QuestionModel;
import topicpromptui.ui.model.state.StateModel;
import topicpromptui.core.storagefilesystem.AnswerType;
import topicpromptui.core.storagefilesystem.Interaction;
import topicpromptui.core.storagefilesystem.InteractionId;
import topicpromptui.core.storagefilesystem.InteractionType;
import topicpromptui.core.storagefilesystem.Topic;
import topicpromptui.core.storagefilesystem.TopicId;
import topicpromptui.ui.viewmodel.answer.AnswerVmMediator;
import topicpromptui.ui.viewmodel.answer.AnswerVmModule;
import topicpromptui.ui.viewmodel.history.HistoryVmMediator;
import topicpromptui.ui.viewmodel.question.QuestionVmMediator;
import topicpromptui.ui.viewmodel.topic.TopicVmMediator;
import topicpromptui.ui.viewmodel.ui.TopicPromptUiVmMediator;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import javafx.collections.ObservableMap;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static topicpromptui.core.storagefilesystem.AnswerType.CLAUDE;
import static topicpromptui.core.storagefilesystem.AnswerType.GCP;
import static topicpromptui.core.storagefilesystem.AnswerType.GRAMMAR;
import static topicpromptui.core.storagefilesystem.AnswerType.OPEN_AI;
import static topicpromptui.core.storagefilesystem.InteractionType.QUESTION;
import static javafx.scene.input.KeyCode.DIGIT1;
import static javafx.scene.input.KeyCode.DIGIT2;
import static javafx.scene.input.KeyCode.DIGIT3;
import static javafx.scene.input.KeyCode.DIGIT4;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.F;
import static javafx.scene.input.KeyCode.U;
import static javafx.scene.input.KeyCode.UP;
import static javafx.scene.input.KeyCode.V;
import static javafx.scene.input.KeyCombination.ALT_DOWN;
import static javafx.scene.input.KeyCombination.CONTROL_DOWN;

@Singleton
class MediatorImpl implements HistoryMediator, QuestionMediator, TopicMediator, AnswerMediator,
        TopicPromptUiMediator, TopicPromptUiApplicationMediator {
    private static final Logger log = LoggerFactory.getLogger(MediatorImpl.class);
    private final AnswerVmMediator grammarAnswerVM;
    private final AnswerVmMediator openAiAnswerVM;
    private final AnswerVmMediator claudeAnswerVM;
    private final AnswerVmMediator gcpAnswerVM;
    private final HistoryVmMediator historyVM;
    private final QuestionVmMediator questionVM;
    private final TopicVmMediator topicVM;
    private final TopicPromptUiVmMediator uiVM;
    private final StateModel stateModel;
    private final QuestionModel questionModel;
    private final ClipboardModel clipboardModel;
    private final FileModel fileModel;

    @Inject
    MediatorImpl(@Named(AnswerVmModule.GRAMMAR) AnswerVmMediator grammarAnswerVM,
                 @Named(AnswerVmModule.OPEN_AI) AnswerVmMediator openAiAnswerVM,
                 @Named(AnswerVmModule.CLAUDE) AnswerVmMediator claudeAnswerVM,
                 @Named(AnswerVmModule.GCP) AnswerVmMediator gcpAnswerVM,
                 HistoryVmMediator historyVM,
                 QuestionVmMediator questionVM,
                 TopicVmMediator topicVM,
                 TopicPromptUiVmMediator uiVM,
                 StateModel stateModel,
                 QuestionModel questionModel,
                 ClipboardModel clipboardModel,
                 FileModel fileModel) {
        this.grammarAnswerVM = grammarAnswerVM;
        this.openAiAnswerVM = openAiAnswerVM;
        this.claudeAnswerVM = claudeAnswerVM;
        this.gcpAnswerVM = gcpAnswerVM;
        this.historyVM = historyVM;
        this.questionVM = questionVM;
        this.topicVM = topicVM;
        this.uiVM = uiVM;
        this.stateModel = stateModel;
        this.questionModel = questionModel;
        this.clipboardModel = clipboardModel;
        this.fileModel = fileModel;
    }

    @Override
    public void stageShowed() {
        log.trace("stageShowed");
        grammarAnswerVM.initialize();
        openAiAnswerVM.initialize();
        claudeAnswerVM.initialize();
        gcpAnswerVM.initialize();
        historyVM.displayCurrentInteraction();
        topicVM.initialize();
        topicVM.setLabel();
        topicVM.updateComboBoxSelectedItemFromStateModel();
    }

    @Override
    public void topicWasChosen() {
        log.trace("topicWasChosen");
        if (Boolean.TRUE.equals(stateModel.isHistoryFilteringEnabled())) {
            stateModel.chooseFirstInteractionAsCurrent();
        }
        topicVM.updateComboBoxItems();
        topicVM.updateComboBoxSelectedItemFromStateModel();
        historyVM.displayCurrentInteraction();
        questionVM.displayCurrentInteraction();
        grammarAnswerVM.displayCurrentAnswer();
        openAiAnswerVM.displayCurrentAnswer();
        claudeAnswerVM.displayCurrentAnswer();
        gcpAnswerVM.displayCurrentAnswer();
        questionVM.focusOnQuestionAndSelect();
    }

    private void answerUpdated(InteractionId interactionId, AnswerType answerType) {
        log.trace("answerUpdated");
        // Same guard as answerProgress: a completion for a no-longer-current interaction must
        // not repaint the pane — displayCurrentAnswer would revert a partial answer still
        // streaming for the current interaction to its stored (stale or empty) HTML.
        if (interactionId.equals(stateModel.getCurrentInteractionId())) {
            switch (answerType) {
                case GRAMMAR -> grammarAnswerVM.displayCurrentAnswer();
                case OPEN_AI -> openAiAnswerVM.displayCurrentAnswer();
                case CLAUDE -> claudeAnswerVM.displayCurrentAnswer();
                case GCP -> gcpAnswerVM.displayCurrentAnswer();
            }
        }
        historyVM.displayCurrentInteraction();
    }

    private void answerProgress(InteractionId interactionId, AnswerType answerType, String html) {
        // A stream may still be running after the user navigated to another interaction —
        // its partial output must not overwrite the pane showing the current interaction.
        if (!interactionId.equals(stateModel.getCurrentInteractionId())) {
            return;
        }
        switch (answerType) {
            case GRAMMAR -> grammarAnswerVM.displayPartialAnswer(html);
            case OPEN_AI -> openAiAnswerVM.displayPartialAnswer(html);
            case CLAUDE -> claudeAnswerVM.displayPartialAnswer(html);
            case GCP -> gcpAnswerVM.displayPartialAnswer(html);
        }
    }

    @Override
    public void isTopicFilterHistoryChanged() {
        log.trace("isTopicFilterHistoryChanged");
        if (Boolean.TRUE.equals(stateModel.isHistoryFilteringEnabled())) {
            stateModel.chooseFirstInteractionAsCurrent();
        }
        historyVM.displayCurrentInteraction();
        grammarAnswerVM.displayCurrentAnswer();
        openAiAnswerVM.displayCurrentAnswer();
        claudeAnswerVM.displayCurrentAnswer();
        gcpAnswerVM.displayCurrentAnswer();
    }

    @Override
    public void displayCurrentInteraction() {
        log.trace("displayCurrentInteraction");
        historyVM.displayCurrentInteraction();
        questionVM.displayCurrentInteraction();
        topicVM.updateComboBoxItems();
        topicVM.updateComboBoxSelectedItemFromCurrentInteraction();
        grammarAnswerVM.displayCurrentAnswer();
        openAiAnswerVM.displayCurrentAnswer();
        claudeAnswerVM.displayCurrentAnswer();
        gcpAnswerVM.displayCurrentAnswer();
    }

    @Override
    public void addShortcuts(ObservableMap<KeyCombination, Runnable> accelerators) {
        log.trace("addShortcuts");
        accelerators.put(new KeyCodeCombination(UP, CONTROL_DOWN, ALT_DOWN), this::selectPreviousHistoryItem);
        accelerators.put(new KeyCodeCombination(DOWN, CONTROL_DOWN, ALT_DOWN), this::selectNextHistoryItem);
        accelerators.put(new KeyCodeCombination(V, CONTROL_DOWN, ALT_DOWN), questionVM::pasteQuestionFromClipboard);
        accelerators.put(new KeyCodeCombination(ESCAPE), this::escapePressed);
        accelerators.put(new KeyCodeCombination(ENTER, CONTROL_DOWN), () -> questionVM.createNewInteractionAndRequestAnswers(QUESTION));
        accelerators.put(new KeyCodeCombination(U, ALT_DOWN), questionVM::toggleFollowUp);
        accelerators.put(new KeyCodeCombination(F, CONTROL_DOWN), historyVM::focusOnFilterAndSelect);
        // Digit order must match the Alt-1..4 copy shortcuts (AnswerVmImpl.hotkeyDigitMap)
        accelerators.put(new KeyCodeCombination(DIGIT1, CONTROL_DOWN), () -> uiVM.toggleExpandedAnswer(GRAMMAR));
        accelerators.put(new KeyCodeCombination(DIGIT2, CONTROL_DOWN), () -> uiVM.toggleExpandedAnswer(OPEN_AI));
        accelerators.put(new KeyCodeCombination(DIGIT3, CONTROL_DOWN), () -> uiVM.toggleExpandedAnswer(CLAUDE));
        accelerators.put(new KeyCodeCombination(DIGIT4, CONTROL_DOWN), () -> uiVM.toggleExpandedAnswer(GCP));
    }

    private void escapePressed() {
        if (uiVM.isAnswerExpanded()) {
            uiVM.collapseExpandedAnswer();
        } else {
            questionVM.focusOnQuestionAndSelect();
        }
    }

    @Override
    public void selectPreviousHistoryItem() {
        historyVM.selectPreviousItem();
    }

    @Override
    public void selectNextHistoryItem() {
        historyVM.selectNextItem();
    }

    @Override
    public void focusHistoryFilter() {
        historyVM.focusOnFilterAndSelect();
    }

    @Override
    public InteractionId getCurrentInteractionId() {
        return stateModel.getCurrentInteractionId();
    }

    @Override
    public Interaction getCurrentInteraction() {
        return getCurrentInteractionOpt().orElseThrow();
    }

    @Override
    public Optional<Interaction> getCurrentInteractionOpt() {
        return stateModel.getCurrentInteractionOpt();
    }

    @Override
    public void setCurrentInteractionId(InteractionId currentInteractionId) {
        stateModel.setCurrentInteractionId(currentInteractionId);
    }

    @Override
    public void deleteCurrentInteraction() {
        stateModel.deleteCurrentInteraction();
    }

    @Override
    public List<Interaction> getFullHistory() {
        return stateModel.getFullHistory();
    }

    @Override
    public List<Interaction> getFilteredHistory() {
        return stateModel.getFilteredHistory();
    }

    @Override
    public String getHistoryFilterText() {
        return stateModel.getHistoryFilterText();
    }

    @Override
    public void setHistoryFilterText(String filterText) {
        stateModel.setHistoryFilterText(filterText);
    }

    @Override
    public Topic getCurrentTopic() {
        return stateModel.getCurrentTopic();
    }

    @Override
    public Topic getTopic(TopicId topicId) {
        return stateModel.getTopic(topicId);
    }

    @Override
    public Boolean isHistoryFilteringEnabled() {
        return stateModel.isHistoryFilteringEnabled();
    }

    @Override
    public void setIsHistoryFilteringEnabled(Boolean isHistoryFilteringEnabled) {
        stateModel.setIsHistoryFilteringEnabled(isHistoryFilteringEnabled);
    }

    @Override
    public void setCurrentTopic(Topic currentTopic) {
        stateModel.setCurrentTopic(currentTopic);
    }

    @Override
    public List<Topic> getTopics() {
        return stateModel.getTopics();
    }

    @Override
    public Topic addTopic(String topic) {
        return stateModel.addTopic(topic);
    }

    @Override
    public Topic renameTopic(TopicId topicId, String newTitle) {
        return stateModel.renameTopic(topicId, newTitle);
    }

    @Override
    public void deleteTopic(TopicId topicId) {
        stateModel.deleteTopic(topicId);
    }

    @Override
    public Long getInteractionCountInTopic(String topic) {
        return stateModel.getInteractionCountInTopic(topic);
    }

    @Override
    public void setEditedQuestion(String question) {
        stateModel.setEditedQuestion(question);
    }

    @Override
    public Boolean isEnteringNewQuestion() {
        return stateModel.isEnteringNewQuestion();
    }

    @Override
    public void requestAnswer(InteractionId interactionId, AnswerType answerType) {
        questionModel.requestAnswer(interactionId, answerType, () -> answerUpdated(interactionId, answerType),
                html -> answerProgress(interactionId, answerType, html));
    }

    @Override
    public void toggleExpandedAnswer(AnswerType answerType) {
        log.trace("toggleExpandedAnswer: {}", answerType);
        uiVM.toggleExpandedAnswer(answerType);
    }

    @Override
    public InteractionId createInteraction(InteractionType interactionType, InteractionId parentInteractionId) {
        var interaction = stateModel.createInteraction(interactionType, parentInteractionId);
        topicVM.updateComboBoxItems();
        grammarAnswerVM.displayCurrentAnswer();
        openAiAnswerVM.displayCurrentAnswer();
        claudeAnswerVM.displayCurrentAnswer();
        gcpAnswerVM.displayCurrentAnswer();
        return interaction;
    }

    @Override
    public String getTextFromClipboard() {
        return clipboardModel.getTextFromClipboard();
    }

    @Override
    public void putHtmlToClipboard(String html) {
        clipboardModel.putHtmlToClipboard(html);
    }

    @Override
    public void chooseFirstInteractionAsCurrent() {
        stateModel.chooseFirstInteractionAsCurrent();
    }

    @Override
    public void chooseFirstTopicAsCurrent() {
        stateModel.setFirstTopicAsCurrent();
    }

    @Override
    public InputStream getAppIcon() {
        return fileModel.getAppIcon();
    }

    @Override
    public String getAppVersion() {
        return fileModel.getAppVersion();
    }

    @Override
    public URL getFxmlLocation() {
        return fileModel.getFxmlLocation();
    }
}

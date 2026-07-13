package gptui.ui.viewmodel.mediator;

import gptui.ui.model.clipboard.ClipboardModel;
import gptui.ui.model.file.FileModel;
import gptui.ui.model.question.QuestionModel;
import gptui.ui.model.state.StateModel;
import gptui.core.storagefilesystem.AnswerType;
import gptui.core.storagefilesystem.Interaction;
import gptui.core.storagefilesystem.InteractionId;
import gptui.core.storagefilesystem.InteractionType;
import gptui.core.storagefilesystem.Theme;
import gptui.core.storagefilesystem.ThemeId;
import gptui.ui.viewmodel.answer.AnswerVmMediator;
import gptui.ui.viewmodel.answer.AnswerVmModule;
import gptui.ui.viewmodel.history.HistoryVmMediator;
import gptui.ui.viewmodel.question.QuestionVmMediator;
import gptui.ui.viewmodel.theme.ThemeVmMediator;
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

import static gptui.core.storagefilesystem.InteractionType.QUESTION;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.U;
import static javafx.scene.input.KeyCode.UP;
import static javafx.scene.input.KeyCode.V;
import static javafx.scene.input.KeyCombination.ALT_DOWN;
import static javafx.scene.input.KeyCombination.CONTROL_DOWN;

@Singleton
class MediatorImpl implements HistoryMediator, QuestionMediator, ThemeMediator, AnswerMediator,
        GptUiMediator, GptUiApplicationMediator {
    private static final Logger log = LoggerFactory.getLogger(MediatorImpl.class);
    @Inject
    @Named(AnswerVmModule.GRAMMAR)
    private AnswerVmMediator grammarAnswerVM;
    @Inject
    @Named(AnswerVmModule.OPEN_AI)
    private AnswerVmMediator openAiAnswerVM;
    @Inject
    @Named(AnswerVmModule.CLAUDE)
    private AnswerVmMediator claudeAnswerVM;
    @Inject
    @Named(AnswerVmModule.GCP)
    private AnswerVmMediator gcpAnswerVM;
    @Inject
    private HistoryVmMediator historyVM;
    @Inject
    private QuestionVmMediator questionVM;
    @Inject
    private ThemeVmMediator themeVM;
    @Inject
    private StateModel stateModel;
    @Inject
    private QuestionModel questionModel;
    @Inject
    private ClipboardModel clipboardModel;
    @Inject
    private FileModel fileModel;

    @Override
    public void stageShowed() {
        log.trace("stageShowed");
        grammarAnswerVM.initialize();
        openAiAnswerVM.initialize();
        claudeAnswerVM.initialize();
        gcpAnswerVM.initialize();
        historyVM.displayCurrentInteraction();
        themeVM.initialize();
        themeVM.setLabel();
        themeVM.updateComboBoxSelectedItemFromStateModel();
    }

    @Override
    public void themeWasChosen() {
        log.trace("themeWasChosen");
        if (Boolean.TRUE.equals(stateModel.isHistoryFilteringEnabled())) {
            stateModel.chooseFirstInteractionAsCurrent();
        }
        themeVM.updateComboBoxItems();
        themeVM.updateComboBoxSelectedItemFromStateModel();
        historyVM.displayCurrentInteraction();
        questionVM.displayCurrentInteraction();
        grammarAnswerVM.displayCurrentAnswer();
        openAiAnswerVM.displayCurrentAnswer();
        claudeAnswerVM.displayCurrentAnswer();
        gcpAnswerVM.displayCurrentAnswer();
        questionVM.focusOnQuestionAndSelect();
    }

    private void answerUpdated(AnswerType answerType) {
        log.trace("answerUpdated");
        switch (answerType) {
            case GRAMMAR -> grammarAnswerVM.displayCurrentAnswer();
            case OPEN_AI -> openAiAnswerVM.displayCurrentAnswer();
            case CLAUDE -> claudeAnswerVM.displayCurrentAnswer();
            case GCP -> gcpAnswerVM.displayCurrentAnswer();
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
    public void isThemeFilterHistoryChanged() {
        log.trace("isThemeFilterHistoryChanged");
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
        themeVM.updateComboBoxItems();
        themeVM.updateComboBoxSelectedItemFromCurrentInteraction();
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
        accelerators.put(new KeyCodeCombination(V, CONTROL_DOWN, ALT_DOWN), () -> questionVM.pasteQuestionFromClipboard());
        accelerators.put(new KeyCodeCombination(ESCAPE), () -> questionVM.focusOnQuestionAndSelect());
        accelerators.put(new KeyCodeCombination(ENTER, CONTROL_DOWN), () -> questionVM.createNewInteractionAndRequestAnswers(QUESTION));
        accelerators.put(new KeyCodeCombination(U, ALT_DOWN), questionVM::toggleFollowUp);
    }

    void selectPreviousHistoryItem() {
        historyVM.selectPreviousItem();
    }

    @Override
    public void selectNextHistoryItem() {
        historyVM.selectNextItem();
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
    public Theme getCurrentTheme() {
        return stateModel.getCurrentTheme();
    }

    @Override
    public Theme getTheme(ThemeId themeId) {
        return stateModel.getTheme(themeId);
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
    public void setCurrentTheme(Theme currentTheme) {
        stateModel.setCurrentTheme(currentTheme);
    }

    @Override
    public List<Theme> getThemes() {
        return stateModel.getThemes();
    }

    @Override
    public Theme addTheme(String theme) {
        return stateModel.addTheme(theme);
    }

    @Override
    public Theme renameTheme(ThemeId themeId, String newTitle) {
        return stateModel.renameTheme(themeId, newTitle);
    }

    @Override
    public Long getInteractionCountInTheme(String theme) {
        return stateModel.getInteractionCountInTheme(theme);
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
        questionModel.requestAnswer(interactionId, answerType, () -> answerUpdated(answerType),
                html -> answerProgress(interactionId, answerType, html));
    }

    @Override
    public InteractionId createInteraction(InteractionType interactionType, InteractionId parentInteractionId) {
        var interaction = stateModel.createInteraction(interactionType, parentInteractionId);
        themeVM.updateComboBoxItems();
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
    public void chooseFirstThemeAsCurrent() {
        stateModel.setFirstThemeAsCurrent();
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

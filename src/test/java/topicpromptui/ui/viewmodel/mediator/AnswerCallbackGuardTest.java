package topicpromptui.ui.viewmodel.mediator;

import topicpromptui.core.storagefilesystem.InteractionId;
import topicpromptui.ui.model.clipboard.ClipboardModel;
import topicpromptui.ui.model.file.FileModel;
import topicpromptui.ui.model.question.QuestionModel;
import topicpromptui.ui.model.state.StateModel;
import topicpromptui.ui.viewmodel.answer.AnswerVmMediator;
import topicpromptui.ui.viewmodel.history.HistoryVmMediator;
import topicpromptui.ui.viewmodel.question.QuestionVmMediator;
import topicpromptui.ui.viewmodel.topic.TopicVmMediator;
import topicpromptui.ui.viewmodel.ui.TopicPromptUiVmMediator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Consumer;

import static topicpromptui.core.storagefilesystem.AnswerType.GCP;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A stream may still be running (or complete) after the user navigated to another interaction;
 * neither its partial output nor its completion may repaint the pane showing the current
 * interaction. The completion/progress callbacks MediatorImpl hands to QuestionModel are
 * captured from a mock and invoked directly, so no JavaFX toolkit is needed.
 */
class AnswerCallbackGuardTest {
    private final AnswerVmMediator grammarAnswerVM = mock(AnswerVmMediator.class);
    private final AnswerVmMediator openAiAnswerVM = mock(AnswerVmMediator.class);
    private final AnswerVmMediator claudeAnswerVM = mock(AnswerVmMediator.class);
    private final AnswerVmMediator gcpAnswerVM = mock(AnswerVmMediator.class);
    private final HistoryVmMediator historyVM = mock(HistoryVmMediator.class);
    private final StateModel stateModel = mock(StateModel.class);
    private final QuestionModel questionModel = mock(QuestionModel.class);
    private final MediatorImpl mediator = new MediatorImpl(grammarAnswerVM, openAiAnswerVM, claudeAnswerVM,
            gcpAnswerVM, historyVM, mock(QuestionVmMediator.class), mock(TopicVmMediator.class),
            mock(TopicPromptUiVmMediator.class), stateModel, questionModel,
            mock(ClipboardModel.class), mock(FileModel.class));

    private final InteractionId streamedId = new InteractionId(1L);
    private final InteractionId otherId = new InteractionId(2L);

    @Test
    void completionForNonCurrentInteractionSkipsPaneButRefreshesHistory() {
        when(stateModel.getCurrentInteractionId()).thenReturn(otherId);
        captureCallbacks().completion().run();
        verify(gcpAnswerVM, never()).displayCurrentAnswer();
        verify(historyVM).displayCurrentInteraction();
    }

    @Test
    void completionForCurrentInteractionRefreshesPane() {
        when(stateModel.getCurrentInteractionId()).thenReturn(streamedId);
        captureCallbacks().completion().run();
        verify(gcpAnswerVM).displayCurrentAnswer();
        verify(historyVM).displayCurrentInteraction();
    }

    @Test
    void progressForNonCurrentInteractionIsDropped() {
        when(stateModel.getCurrentInteractionId()).thenReturn(otherId);
        captureCallbacks().progress().accept("<p>partial</p>");
        verify(gcpAnswerVM, never()).displayPartialAnswer(any());
    }

    @Test
    void progressForCurrentInteractionIsDisplayed() {
        when(stateModel.getCurrentInteractionId()).thenReturn(streamedId);
        captureCallbacks().progress().accept("<p>partial</p>");
        verify(gcpAnswerVM).displayPartialAnswer("<p>partial</p>");
    }

    private record Callbacks(Runnable completion, Consumer<String> progress) {
    }

    @SuppressWarnings("unchecked")
    private Callbacks captureCallbacks() {
        mediator.requestAnswer(streamedId, GCP);
        var completion = ArgumentCaptor.forClass(Runnable.class);
        var progress = ArgumentCaptor.forClass(Consumer.class);
        verify(questionModel).requestAnswer(eq(streamedId), eq(GCP), completion.capture(), progress.capture());
        return new Callbacks(completion.getValue(), progress.getValue());
    }
}

package topicpromptui.ui.viewmodel.mediator;

import org.junit.jupiter.api.Test;
import topicpromptui.ui.model.aiprovider.AiProviderSelectionModel;
import topicpromptui.ui.model.clipboard.ClipboardModel;
import topicpromptui.ui.model.file.FileModel;
import topicpromptui.ui.model.question.QuestionModel;
import topicpromptui.ui.model.state.StateModel;
import topicpromptui.ui.viewmodel.answer.AnswerVmMediator;
import topicpromptui.ui.viewmodel.history.HistoryVmMediator;
import topicpromptui.ui.viewmodel.question.QuestionVmMediator;
import topicpromptui.ui.viewmodel.topic.TopicVmMediator;
import topicpromptui.ui.viewmodel.ui.TopicPromptUiVmMediator;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static topicpromptui.core.domain.AiProvider.CLAUDE;
import static topicpromptui.core.domain.AiProvider.GCP;
import static topicpromptui.core.domain.AiProvider.OPEN_AI;
import static topicpromptui.core.domain.AnswerType.AI_1;
import static topicpromptui.core.domain.AnswerType.AI_2;
import static topicpromptui.core.domain.AnswerType.AI_3;
import static topicpromptui.core.domain.AnswerType.GRAMMAR;

/**
 * A pane's provider is runtime state, so the mediator must read it per pane and, on a change, refresh
 * only the pane that changed.
 */
class AnswerProviderSelectionTest {
    private final AiProviderSelectionModel providerSelection = mock(AiProviderSelectionModel.class);
    private final AnswerVmMediator ai1AnswerVM = mock(AnswerVmMediator.class);
    private final AnswerVmMediator ai2AnswerVM = mock(AnswerVmMediator.class);
    private final MediatorImpl mediator = new MediatorImpl(
            Map.of(GRAMMAR, mock(AnswerVmMediator.class), AI_1, ai1AnswerVM,
                    AI_2, ai2AnswerVM, AI_3, mock(AnswerVmMediator.class)),
            mock(HistoryVmMediator.class),
            mock(QuestionVmMediator.class), mock(TopicVmMediator.class), mock(TopicPromptUiVmMediator.class),
            mock(StateModel.class), mock(QuestionModel.class), mock(ClipboardModel.class), mock(FileModel.class),
            providerSelection);

    @Test
    void providerComesFromTheSelectionForThatPane() {
        when(providerSelection.getProvider(AI_1)).thenReturn(OPEN_AI);
        when(providerSelection.getProvider(AI_2)).thenReturn(CLAUDE);
        assertThat(mediator.getAnswerProvider(AI_1)).isEqualTo(OPEN_AI);
        assertThat(mediator.getAnswerProvider(AI_2)).isEqualTo(CLAUDE);
    }

    @Test
    void availableProvidersComeFromTheSelectionModel() {
        when(providerSelection.getAvailableProviders()).thenReturn(List.of(OPEN_AI, CLAUDE, GCP));
        assertThat(mediator.getAvailableProviders()).containsExactly(OPEN_AI, CLAUDE, GCP);
    }

    @Test
    void settingAProviderRefreshesOnlyThatPane() {
        mediator.setAnswerProvider(AI_2, CLAUDE);
        verify(providerSelection).setProvider(AI_2, CLAUDE);
        verify(ai2AnswerVM).refreshProvider();
        verify(ai1AnswerVM, never()).refreshProvider();
    }
}

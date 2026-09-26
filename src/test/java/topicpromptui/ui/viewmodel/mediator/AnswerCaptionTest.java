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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static topicpromptui.core.domain.AiProvider.CLAUDE;
import static topicpromptui.core.domain.AiProvider.OPEN_AI;
import static topicpromptui.core.domain.AnswerType.AI_1;
import static topicpromptui.core.domain.AnswerType.AI_2;

/**
 * A pane's caption is the display name of whichever provider is selected for it, so it must follow a
 * selection change instead of being fixed per pane.
 */
class AnswerCaptionTest {
    private final AiProviderSelectionModel providerSelection = mock(AiProviderSelectionModel.class);
    private final MediatorImpl mediator = new MediatorImpl(mock(AnswerVmMediator.class), mock(AnswerVmMediator.class),
            mock(AnswerVmMediator.class), mock(AnswerVmMediator.class), mock(HistoryVmMediator.class),
            mock(QuestionVmMediator.class), mock(TopicVmMediator.class), mock(TopicPromptUiVmMediator.class),
            mock(StateModel.class), mock(QuestionModel.class), mock(ClipboardModel.class), mock(FileModel.class),
            providerSelection);

    @Test
    void captionComesFromTheProviderSelectedForThePane() {
        when(providerSelection.getProvider(AI_1)).thenReturn(OPEN_AI);
        when(providerSelection.getProvider(AI_2)).thenReturn(CLAUDE);
        assertThat(mediator.getAnswerCaption(AI_1)).isEqualTo("OpenAI:");
        assertThat(mediator.getAnswerCaption(AI_2)).isEqualTo("Claude:");
    }
}

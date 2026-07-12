package gptui.ui.model.question.question;

import gptui.BaseTest;
import gptui.core.ai.ConversationTurn;
import gptui.core.storagefilesystem.Answer;
import gptui.core.storagefilesystem.AnswerState;
import gptui.core.storagefilesystem.Interaction;
import gptui.core.storagefilesystem.InteractionId;
import gptui.core.storagefilesystem.InteractionType;
import gptui.ui.model.storage.StorageModel;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static gptui.core.ai.ConversationTurn.Speaker.MODEL;
import static gptui.core.ai.ConversationTurn.Speaker.USER;
import static gptui.core.storagefilesystem.AnswerType.OPEN_AI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FollowUpHistoryBuilderTest extends BaseTest {
    private final FollowUpHistoryBuilder builder = injector.getInstance(FollowUpHistoryBuilder.class);
    private final StorageModel storage = injector.getInstance(StorageModel.class);

    @Test
    void buildHistoryWalksChainOldestFirst() {
        var theme = storage.addTheme("Theme");
        var rootId = new InteractionId(1L);
        storage.saveInteraction(new Interaction(rootId, InteractionType.QUESTION, theme.id(), "root question",
                Map.of(OPEN_AI, new Answer(OPEN_AI, "root prompt", "root answer", "<p>root answer</p>", AnswerState.SUCCESS, null,
                        null, null, null, null, null)),
                null));

        var midId = new InteractionId(2L);
        storage.saveInteraction(new Interaction(midId, InteractionType.QUESTION, theme.id(), "mid question",
                Map.of(OPEN_AI, new Answer(OPEN_AI, "mid prompt", "mid answer", "<p>mid answer</p>", AnswerState.SUCCESS, null,
                        null, null, null, null, null)),
                rootId));

        var turns = builder.buildHistory(midId, OPEN_AI);

        assertThat(turns).containsExactly(
                new ConversationTurn(USER, "root prompt"),
                new ConversationTurn(MODEL, "root answer"),
                new ConversationTurn(USER, "mid prompt"),
                new ConversationTurn(MODEL, "mid answer"));
    }

    @Test
    void buildHistoryThrowsWhenAncestorAnswerMissing() {
        var theme = storage.addTheme("Theme 2");
        var rootId = storage.newInteractionId();
        storage.saveInteraction(new Interaction(rootId, InteractionType.QUESTION, theme.id(), "root question",
                Map.of(), null));

        assertThatThrownBy(() -> builder.buildHistory(rootId, OPEN_AI))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void buildHistoryThrowsWhenAncestorAnswerNotSuccess() {
        var theme = storage.addTheme("Theme 3");
        var rootId = storage.newInteractionId();
        storage.saveInteraction(new Interaction(rootId, InteractionType.QUESTION, theme.id(), "root question",
                Map.of(OPEN_AI, new Answer(OPEN_AI, "p", "a", "<p>a</p>", AnswerState.FAIL, null,
                        null, null, null, null, null)), null));

        assertThatThrownBy(() -> builder.buildHistory(rootId, OPEN_AI))
                .isInstanceOf(IllegalStateException.class);
    }
}

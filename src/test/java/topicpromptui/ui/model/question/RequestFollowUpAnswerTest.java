package topicpromptui.ui.model.question;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import topicpromptui.RootModule;
import topicpromptui.TestRootModule;
import topicpromptui.core.ai.openai.MockOpenAiApi;
import topicpromptui.core.storagefilesystem.Answer;
import topicpromptui.core.storagefilesystem.AnswerState;
import topicpromptui.core.storagefilesystem.AnswerType;
import topicpromptui.core.storagefilesystem.Interaction;
import topicpromptui.core.storagefilesystem.InteractionId;
import topicpromptui.core.storagefilesystem.InteractionType;
import topicpromptui.ui.model.storage.StorageModel;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.time.Duration;
import java.util.Map;

import static topicpromptui.core.storagefilesystem.AnswerState.NEW;
import static topicpromptui.core.storagefilesystem.AnswerState.SENT;
import static topicpromptui.core.storagefilesystem.AnswerType.GRAMMAR;
import static topicpromptui.core.storagefilesystem.AnswerType.OPEN_AI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

class RequestFollowUpAnswerTest extends ApplicationTest {
    // Needs a live JavaFX toolkit, since QuestionModelImpl.updateAnswer calls Platform.runLater; NOSONAR
    // extend ApplicationTest directly (like ClipboardModelTest) rather than BaseTest, without
    // booting the full TopicPromptUiApplication/FXML scene like BaseTopicPromptUiTest does.
    private final Injector injector = Guice.createInjector(Modules.override(new RootModule()).with(new TestRootModule()));
    private final QuestionModel questionModel = injector.getInstance(QuestionModel.class);
    private final StorageModel storage = injector.getInstance(StorageModel.class);
    private final MockOpenAiApi openAiApi = injector.getInstance(MockOpenAiApi.class);

    @Test
    void requestFollowUpAnswerSendsFullConversationHistory() {
        var topic = storage.addTopic("Topic 1");
        var parentId = new InteractionId(1L);
        storage.saveInteraction(new Interaction(parentId, InteractionType.QUESTION, topic.id(), "What is Java?",
                Map.of(OPEN_AI, new Answer(OPEN_AI, "Explain Java briefly", "Java is a language.",
                        "<p>Java is a language.</p>", AnswerState.SUCCESS, "resp_1",
                        null, null, null, null, null, null)),
                null));

        var followUpId = new InteractionId(2L);
        storage.saveInteraction(new Interaction(followUpId, InteractionType.QUESTION, topic.id(), "Who created it?",
                Map.of(OPEN_AI, new Answer(OPEN_AI, "", "", "", AnswerState.NEW, null,
                        null, null, null, null, null, null)),
                parentId));

        openAiApi.clear().putResponse("Who created it?", "James Gosling created Java.", Duration.ZERO);

        questionModel.requestFollowUpAnswer(followUpId, OPEN_AI, () -> {
        });
        awaitTerminalState(followUpId, OPEN_AI);

        var answer = storage.readInteraction(followUpId).orElseThrow().getAnswer(OPEN_AI).orElseThrow();
        assertThat(answer.answerState()).isEqualTo(AnswerState.SUCCESS);
        assertThat(answer.prompt()).isEqualTo("Who created it?");
        assertThat(answer.answerMd()).isEqualTo("James Gosling created Java.");

        var turns = openAiApi.getTurnsSendHistory().getLast();
        assertThat(turns).hasSize(3);
        assertThat(turns.get(0).content()).isEqualTo("Explain Java briefly");
        assertThat(turns.get(1).content()).isEqualTo("Java is a language.");
        assertThat(turns.get(2).content()).isEqualTo("Who created it?");
    }

    @Test
    void requestFollowUpAnswerRequiresParentInteractionId() {
        var topic = storage.addTopic("Topic 2");
        var standaloneId = storage.newInteractionId();
        storage.saveInteraction(new Interaction(standaloneId, InteractionType.QUESTION, topic.id(), "A question",
                Map.of(OPEN_AI, new Answer(OPEN_AI, "", "", "", AnswerState.NEW, null,
                        null, null, null, null, null, null)), null));

        assertThatThrownBy(() -> questionModel.requestFollowUpAnswer(standaloneId, OPEN_AI, () -> {
        })).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requestFollowUpAnswerGrammarUnsupported() {
        var topic = storage.addTopic("Topic 3");
        var parentId = new InteractionId(1L);
        storage.saveInteraction(new Interaction(parentId, InteractionType.QUESTION, topic.id(), "Q1",
                Map.of(GRAMMAR, new Answer(GRAMMAR, "p1", "a1", "<p>a1</p>", AnswerState.SUCCESS, null,
                        null, null, null, null, null, null)), null));

        var followUpId = new InteractionId(2L);
        storage.saveInteraction(new Interaction(followUpId, InteractionType.QUESTION, topic.id(), "Q2",
                Map.of(GRAMMAR, new Answer(GRAMMAR, "", "", "", AnswerState.NEW, null,
                        null, null, null, null, null, null)), parentId));

        questionModel.requestFollowUpAnswer(followUpId, GRAMMAR, () -> {
        });
        awaitTerminalState(followUpId, GRAMMAR);

        var answer = storage.readInteraction(followUpId).orElseThrow().getAnswer(GRAMMAR).orElseThrow();
        assertThat(answer.answerState()).isEqualTo(AnswerState.FAIL);
        assertThat(answer.answerMd()).contains("follow-up");
    }

    private void awaitTerminalState(InteractionId interactionId, AnswerType answerType) {
        await().atMost(Duration.ofSeconds(5)).until(() ->
                storage.readInteraction(interactionId)
                        .flatMap(interaction -> interaction.getAnswer(answerType))
                        .map(answer -> answer.answerState() != NEW && answer.answerState() != SENT)
                        .orElse(false));
    }
}

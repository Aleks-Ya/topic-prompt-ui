package gptui.ui.model.question;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import gptui.RootModule;
import gptui.TestRootModule;
import gptui.core.ai.openai.MockOpenAiApi;
import gptui.core.storagefilesystem.Answer;
import gptui.core.storagefilesystem.AnswerState;
import gptui.core.storagefilesystem.AnswerType;
import gptui.core.storagefilesystem.Interaction;
import gptui.core.storagefilesystem.InteractionId;
import gptui.core.storagefilesystem.InteractionType;
import gptui.ui.model.storage.StorageModel;
import javafx.application.Platform;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static gptui.core.storagefilesystem.AnswerState.NEW;
import static gptui.core.storagefilesystem.AnswerState.SENT;
import static gptui.core.storagefilesystem.AnswerType.OPEN_AI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class RequestAnswerStreamingTest extends ApplicationTest {
    // Needs a live JavaFX toolkit, since QuestionModelImpl.updateAnswer and the progress
    // throttler call Platform.runLater; extend ApplicationTest directly (like
    // RequestFollowUpAnswerTest) rather than booting the full GptUiApplication/FXML scene.
    private final Injector injector = Guice.createInjector(Modules.override(new RootModule()).with(new TestRootModule()));
    private final QuestionModel questionModel = injector.getInstance(QuestionModel.class);
    private final StorageModel storage = injector.getInstance(StorageModel.class);
    private final MockOpenAiApi openAiApi = injector.getInstance(MockOpenAiApi.class);

    @Test
    void progressHtmlReceivesCumulativeSnapshotsOnFxThreadBeforeFinalAnswer() {
        var interactionId = saveFollowUpInteraction("Who created it?");
        // Per-chunk delay larger than the 250 ms throttle interval so every chunk produces a tick.
        openAiApi.clear().putStreamingResponse("Who created it?",
                List.of("James ", "Gosling ", "created Java."), Duration.ofMillis(300));

        var snapshots = new CopyOnWriteArrayList<String>();
        var onFxThread = new CopyOnWriteArrayList<Boolean>();
        questionModel.requestAnswer(interactionId, OPEN_AI, () -> {
        }, html -> {
            snapshots.add(html);
            onFxThread.add(Platform.isFxApplicationThread());
        });
        awaitTerminalState(interactionId, OPEN_AI);

        await().atMost(Duration.ofSeconds(5)).until(() -> snapshots.size() >= 3);
        assertThat(onFxThread).allMatch(Boolean::booleanValue);
        assertThat(snapshots.get(0)).contains("James");
        assertThat(snapshots.get(1)).contains("James Gosling");
        assertThat(snapshots.getLast()).contains("James Gosling created Java.");

        var answer = storage.readInteraction(interactionId).orElseThrow().getAnswer(OPEN_AI).orElseThrow();
        assertThat(answer.answerState()).isEqualTo(AnswerState.SUCCESS);
        assertThat(answer.answerMd()).isEqualTo("James Gosling created Java.");
    }

    @Test
    void storageStaysSentWhileStreamingAndNoPartialTextIsPersisted() {
        var interactionId = saveFollowUpInteraction("Slow question?");
        openAiApi.clear().putStreamingResponse("Slow question?",
                List.of("chunk1 ", "chunk2"), Duration.ofMillis(400));

        var snapshots = new CopyOnWriteArrayList<String>();
        questionModel.requestAnswer(interactionId, OPEN_AI, () -> {
        }, snapshots::add);

        // While at least one chunk has streamed but the send hasn't finished, storage must be SENT
        // with no partial answer text.
        await().atMost(Duration.ofSeconds(5)).until(() -> !snapshots.isEmpty());
        var midStream = storage.readInteraction(interactionId).orElseThrow().getAnswer(OPEN_AI).orElseThrow();
        assertThat(midStream.answerState()).isEqualTo(SENT);
        assertThat(midStream.answerMd()).isEmpty();

        awaitTerminalState(interactionId, OPEN_AI);
        var answer = storage.readInteraction(interactionId).orElseThrow().getAnswer(OPEN_AI).orElseThrow();
        assertThat(answer.answerState()).isEqualTo(AnswerState.SUCCESS);
        assertThat(answer.answerMd()).isEqualTo("chunk1 chunk2");
    }

    @Test
    void failedRequestPersistsNoPartialText() {
        var interactionId = saveFollowUpInteraction("Unmatched question?");
        openAiApi.clear(); // no mock response registered -> send throws

        var snapshots = new CopyOnWriteArrayList<String>();
        questionModel.requestAnswer(interactionId, OPEN_AI, () -> {
        }, snapshots::add);
        awaitTerminalState(interactionId, OPEN_AI);

        var answer = storage.readInteraction(interactionId).orElseThrow().getAnswer(OPEN_AI).orElseThrow();
        assertThat(answer.answerState()).isEqualTo(AnswerState.FAIL);
        assertThat(snapshots).isEmpty();
    }

    // A follow-up interaction sends the raw question text (no FreeMarker template), which lets
    // the mock match on the question substring directly. Explicit IDs, because
    // storage.newInteractionId() is time-based and collides when called twice within a second.
    private InteractionId saveFollowUpInteraction(String question) {
        var topic = storage.addTopic("Topic " + question);
        var parentId = new InteractionId(1L);
        storage.saveInteraction(new Interaction(parentId, InteractionType.QUESTION, topic.id(), "What is Java?",
                Map.of(OPEN_AI, new Answer(OPEN_AI, "Explain Java briefly", "Java is a language.",
                        "<p>Java is a language.</p>", AnswerState.SUCCESS, "resp_1",
                        null, null, null, null, null, null)),
                null));
        var followUpId = new InteractionId(2L);
        storage.saveInteraction(new Interaction(followUpId, InteractionType.QUESTION, topic.id(), question,
                Map.of(OPEN_AI, new Answer(OPEN_AI, "", "", "", AnswerState.NEW, null,
                        null, null, null, null, null, null)),
                parentId));
        return followUpId;
    }

    private void awaitTerminalState(InteractionId interactionId, AnswerType answerType) {
        await().atMost(Duration.ofSeconds(10)).until(() ->
                storage.readInteraction(interactionId)
                        .flatMap(interaction -> interaction.getAnswer(answerType))
                        .map(answer -> answer.answerState() != NEW && answer.answerState() != SENT)
                        .orElse(false));
    }
}

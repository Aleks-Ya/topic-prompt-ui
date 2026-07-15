package gptui.ui.model.question.question;

import gptui.core.ai.AiApi;
import gptui.core.ai.AiResponse;
import gptui.core.ai.ConversationTurn;
import gptui.ui.model.question.QuestionModel;
import gptui.ui.model.question.prompt.PromptFactory;
import gptui.ui.model.question.sound.SoundService;
import gptui.core.storagefilesystem.Answer;
import gptui.core.storagefilesystem.AnswerType;
import gptui.core.storagefilesystem.InteractionId;
import gptui.ui.model.storage.StorageModel;
import gptui.core.util.Mdc;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static gptui.core.ai.ConversationTurn.Speaker.USER;

import static gptui.core.ai.AiModule.CLAUDE_AI;
import static gptui.core.ai.AiModule.GCP_AI;
import static gptui.core.ai.AiModule.OPEN_AI;
import static gptui.core.ai.AiModule.OPEN_AI_GRAMMAR;
import static gptui.core.storagefilesystem.AnswerState.FAIL;
import static gptui.core.storagefilesystem.AnswerState.SENT;
import static gptui.core.storagefilesystem.AnswerState.SUCCESS;
import static gptui.core.storagefilesystem.AnswerType.GRAMMAR;
import static java.util.concurrent.CompletableFuture.runAsync;

@Singleton
class QuestionModelImpl implements QuestionModel {
    private static final Logger log = LoggerFactory.getLogger(QuestionModelImpl.class);
    // Requests to the 4 AiApi providers are network-bound and sent concurrently, so they
    // must not be limited by ForkJoinPool.commonPool()'s CPU-core-based sizing, which can
    // serialize them on machines with few cores (e.g. CI runners).
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static final Duration PROGRESS_INTERVAL = Duration.ofMillis(250);
    private final StorageModel storage;
    private final PromptFactory promptFactory;
    private final AiApi openAiApi;
    private final AiApi openAiGrammarApi;
    private final AiApi gcpApi;
    private final AiApi claudeApi;
    private final SoundService soundService;
    private final FormatConverter formatConverter;
    private final FollowUpHistoryBuilder followUpHistoryBuilder;

    @Inject
    QuestionModelImpl(StorageModel storage, PromptFactory promptFactory,
                       @Named(OPEN_AI) AiApi openAiApi, @Named(OPEN_AI_GRAMMAR) AiApi openAiGrammarApi,
                       @Named(GCP_AI) AiApi gcpApi, @Named(CLAUDE_AI) AiApi claudeApi,
                       SoundService soundService, FormatConverter formatConverter,
                       FollowUpHistoryBuilder followUpHistoryBuilder) {
        this.storage = storage;
        this.promptFactory = promptFactory;
        this.openAiApi = openAiApi;
        this.openAiGrammarApi = openAiGrammarApi;
        this.gcpApi = gcpApi;
        this.claudeApi = claudeApi;
        this.soundService = soundService;
        this.formatConverter = formatConverter;
        this.followUpHistoryBuilder = followUpHistoryBuilder;
    }

    @Override
    public void requestFollowUpAnswer(InteractionId interactionId, AnswerType answerType, Runnable callback,
                                      Consumer<String> progressHtml) {
        log.info("Sending follow-up request for {}...", answerType);
        var interaction = storage.readInteraction(interactionId).orElseThrow();
        var parentInteractionId = interaction.parentInteractionId();
        if (parentInteractionId == null) {
            throw new IllegalStateException("Interaction has no parentInteractionId, it's not a follow-up: " + interactionId);
        }
        // Follow-up messages skip PromptFactory/FreeMarker templating on purpose: the templated
        // framing (topic, instructions) was already established in the first message of the
        // conversation, so a follow-up only needs to send the raw text the user typed.
        var prompt = interaction.question();
        log.trace("Prompt: {}", prompt);
        updateAnswer(interactionId, answerType, answer -> answer
                        .withPrompt(prompt)
                        .withState(SENT),
                callback);
        sendAsync(interactionId, answerType, callback, progressHtml, onTextDelta -> {
            var turns = new ArrayList<>(followUpHistoryBuilder.buildHistory(parentInteractionId, answerType));
            turns.add(new ConversationTurn(USER, prompt));
            return switch (answerType) {
                case GCP -> gcpApi.send(turns, onTextDelta);
                case CLAUDE -> claudeApi.send(turns, onTextDelta);
                case OPEN_AI -> openAiApi.send(turns, onTextDelta);
                case GRAMMAR -> throw new IllegalArgumentException(
                        "Grammar checks don't support follow-up conversations");
            };
        }, "The follow-up answer request finished.");
    }

    @Override
    public void requestAnswer(InteractionId interactionId, AnswerType answerType, Runnable callback,
                              Consumer<String> progressHtml) {
        log.info("Sending request for {}...", answerType);
        var interaction = storage.readInteraction(interactionId).orElseThrow();
        if (interaction.parentInteractionId() != null && answerType != GRAMMAR) {
            requestFollowUpAnswer(interactionId, answerType, callback, progressHtml);
            return;
        }
        var promptOpt = promptFactory.getPrompt(
                interaction.type(),
                storage.getTopic(interaction.topicId()).title(),
                interaction.question(),
                answerType);
        if (promptOpt.isPresent()) {
            var prompt = promptOpt.get();
            log.trace("Prompt: {}", prompt);
            updateAnswer(interactionId, answerType, answer -> answer
                            .withPrompt(prompt)
                            .withState(SENT),
                    callback);
            sendAsync(interactionId, answerType, callback, progressHtml, onTextDelta -> switch (answerType) {
                case GCP -> gcpApi.send(prompt, onTextDelta);
                case CLAUDE -> claudeApi.send(prompt, onTextDelta);
                case OPEN_AI -> openAiApi.send(prompt, onTextDelta);
                case GRAMMAR -> openAiGrammarApi.send(prompt, onTextDelta);
            }, "The short answer request finished.");
        } else {
            log.info("The short answer was skipped.");
        }
    }

    private void sendAsync(InteractionId interactionId, AnswerType answerType, Runnable callback,
                           Consumer<String> progressHtml, Function<Consumer<String>, AiResponse> send,
                           String finishedMessage) {
        runAsync(() -> Mdc.run(interactionId.id(), () -> {
            log.trace("sendAsync");
            var throttler = new ProgressThrottler(progressHtml);
            var response = send.apply(throttler::onTextDelta);
            var answerHtml = formatConverter.markdownToHtml(response.text());
            updateAnswer(interactionId, answerType, answer ->
                    answer.withAnswerMd(response.text()).withAnswerHtml(answerHtml)
                            .withResponseId(response.responseId())
                            .withModelInfo(response.modelId(), response.effortLevel(), response.finishReason(),
                                    response.inputTokens(), response.outputTokens(), response.totalTokens())
                            .withState(SUCCESS), callback);
            soundService.beenOnAnswer(answerType);
            log.info(finishedMessage);
        }), EXECUTOR).handle((res, e) -> {
            if (e != null) {
                log.error("Sending question exception", e);
                Mdc.run(interactionId.id(), () -> {
                    var message = e.getCause().getMessage();
                    updateAnswer(interactionId, answerType, answer ->
                            answer.withAnswerMd(message).withAnswerHtml(message).withState(FAIL), callback);
                    soundService.beenOnAnswer(answerType);
                });
                return e;
            } else {
                return res;
            }
        });
    }

    /**
     * Accumulates streamed markdown deltas and, at most once per {@link #PROGRESS_INTERVAL},
     * renders the partial markdown to HTML and hands it to {@code progressHtml} on the JavaFX
     * Application Thread. All appends happen on the single executor thread that owns the
     * blocking send, so no locking is needed; only the immutable HTML string crosses threads.
     * Partial text is never persisted — the final answer goes through the usual storage path.
     */
    private class ProgressThrottler {
        private final StringBuilder partialMd = new StringBuilder();
        private final Consumer<String> progressHtml;
        private long lastTickNanos;

        private ProgressThrottler(Consumer<String> progressHtml) {
            this.progressHtml = progressHtml;
            // System.nanoTime() has an arbitrary (possibly negative) origin, so start one
            // interval in the past to make the very first delta paint immediately.
            this.lastTickNanos = System.nanoTime() - PROGRESS_INTERVAL.toNanos();
        }

        private void onTextDelta(String delta) {
            partialMd.append(delta);
            var now = System.nanoTime();
            if (now - lastTickNanos >= PROGRESS_INTERVAL.toNanos()) {
                lastTickNanos = now;
                var html = formatConverter.markdownToHtml(partialMd.toString());
                Platform.runLater(() -> progressHtml.accept(html));
            }
        }
    }

    private synchronized void updateAnswer(InteractionId interactionId, AnswerType answerType,
            UnaryOperator<Answer> update, Runnable callback) {
        log.trace("updateAnswer: interactionId={}, answerType={}", interactionId, answerType);
        storage.updateInteraction(interactionId, interaction ->
                interaction.withAnswer(update.apply(interaction.getAnswer(answerType).orElseThrow())));
        Platform.runLater(callback);
    }
}

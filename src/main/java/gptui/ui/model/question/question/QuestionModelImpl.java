package gptui.ui.model.question.question;

import gptui.core.ai.AiApi;
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

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

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
    public void requestFollowUpAnswer(InteractionId interactionId, AnswerType answerType, Runnable callback) {
        log.info("Sending follow-up request for {}...", answerType);
        var interaction = storage.readInteraction(interactionId).orElseThrow();
        var parentInteractionId = interaction.parentInteractionId();
        if (parentInteractionId == null) {
            throw new IllegalStateException("Interaction has no parentInteractionId, it's not a follow-up: " + interactionId);
        }
        // Follow-up messages skip PromptFactory/FreeMarker templating on purpose: the templated
        // framing (theme, instructions) was already established in the first message of the
        // conversation, so a follow-up only needs to send the raw text the user typed.
        var prompt = interaction.question();
        log.trace("Prompt: {}", prompt);
        updateAnswer(interactionId, answerType, answer -> answer
                        .withPrompt(prompt)
                        .withState(SENT),
                callback);
        runAsync(() -> Mdc.run(interactionId, () -> {
            log.trace("requestFollowUpAnswer async");
            var turns = new ArrayList<>(followUpHistoryBuilder.buildHistory(parentInteractionId, answerType));
            turns.add(new ConversationTurn(USER, prompt));
            var response = switch (answerType) {
                case GCP -> gcpApi.send(turns);
                case CLAUDE -> claudeApi.send(turns);
                case OPEN_AI -> openAiApi.send(turns);
                case GRAMMAR -> throw new IllegalArgumentException(
                        "Grammar checks don't support follow-up conversations");
            };
            var answerHtml = formatConverter.markdownToHtml(response.text());
            updateAnswer(interactionId, answerType, answer ->
                    answer.withAnswerMd(response.text()).withAnswerHtml(answerHtml)
                            .withResponseId(response.responseId()).withState(SUCCESS), callback);
            soundService.beenOnAnswer(answerType);
            log.info("The follow-up answer request finished.");
        }), EXECUTOR).handle((res, e) -> {
            if (e != null) {
                log.error("Sending follow-up question exception", e);
                Mdc.run(interactionId, () -> {
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

    @Override
    public void requestAnswer(InteractionId interactionId, AnswerType answerType, Runnable callback) {
        log.info("Sending request for {}...", answerType);
        var interaction = storage.readInteraction(interactionId).orElseThrow();
        if (interaction.parentInteractionId() != null && answerType != GRAMMAR) {
            requestFollowUpAnswer(interactionId, answerType, callback);
            return;
        }
        var promptOpt = promptFactory.getPrompt(
                interaction.type(),
                storage.getTheme(interaction.themeId()).title(),
                interaction.question(),
                answerType);
        if (promptOpt.isPresent()) {
            var prompt = promptOpt.get();
            log.trace("Prompt: {}", prompt);
            updateAnswer(interactionId, answerType, answer -> answer
                            .withPrompt(prompt)
                            .withState(SENT),
                    callback);
            runAsync(() -> Mdc.run(interactionId, () -> {
                log.trace("requestAnswer async");
                var response = switch (answerType) {
                    case GCP -> gcpApi.send(prompt);
                    case CLAUDE -> claudeApi.send(prompt);
                    case OPEN_AI -> openAiApi.send(prompt);
                    case GRAMMAR -> openAiGrammarApi.send(prompt);
                };
                var answerHtml = formatConverter.markdownToHtml(response.text());
                updateAnswer(interactionId, answerType, answer ->
                        answer.withAnswerMd(response.text()).withAnswerHtml(answerHtml)
                                .withResponseId(response.responseId()).withState(SUCCESS), callback);
                soundService.beenOnAnswer(answerType);
                log.info("The short answer request finished.");
            }), EXECUTOR).handle((res, e) -> {
                if (e != null) {
                    log.error("Sending question exception", e);
                    Mdc.run(interactionId, () -> {
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
        } else {
            log.info("The short answer was skipped.");
        }
    }

    private synchronized void updateAnswer(InteractionId interactionId, AnswerType answerType, Function<Answer,
            Answer> update, Runnable callback) {
        log.trace("updateAnswer: interactionId={}, answerType={}", interactionId, answerType);
        storage.updateInteraction(interactionId, interaction ->
                interaction.withAnswer(update.apply(interaction.getAnswer(answerType).orElseThrow())));
        Platform.runLater(callback);
    }
}

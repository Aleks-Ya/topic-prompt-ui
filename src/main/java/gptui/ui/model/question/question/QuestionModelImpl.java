package gptui.ui.model.question.question;

import gptui.core.ai.AiApi;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static gptui.core.ai.AiModule.CLAUDE_AI;
import static gptui.core.ai.AiModule.GCP_AI;
import static gptui.core.ai.AiModule.OPEN_AI;
import static gptui.core.ai.AiModule.OPEN_AI_GRAMMAR;
import static gptui.core.storagefilesystem.AnswerState.FAIL;
import static gptui.core.storagefilesystem.AnswerState.SENT;
import static gptui.core.storagefilesystem.AnswerState.SUCCESS;
import static java.util.concurrent.CompletableFuture.runAsync;

@Singleton
class QuestionModelImpl implements QuestionModel {
    private static final Logger log = LoggerFactory.getLogger(QuestionModelImpl.class);
    // Requests to the 4 AiApi providers are network-bound and sent concurrently, so they
    // must not be limited by ForkJoinPool.commonPool()'s CPU-core-based sizing, which can
    // serialize them on machines with few cores (e.g. CI runners).
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    @Inject
    private StorageModel storage;
    @Inject
    private PromptFactory promptFactory;
    @Inject
    @Named(OPEN_AI)
    private AiApi openAiApi;
    @Inject
    @Named(OPEN_AI_GRAMMAR)
    private AiApi openAiGrammarApi;
    @Inject
    @Named(GCP_AI)
    private AiApi gcpApi;
    @Inject
    @Named(CLAUDE_AI)
    private AiApi claudeApi;
    @Inject
    private SoundService soundService;
    @Inject
    private FormatConverter formatConverter;

    @Override
    public void requestAnswer(InteractionId interactionId, AnswerType answerType, Runnable callback) {
        log.info("Sending request for {}...", answerType);
        var interaction = storage.readInteraction(interactionId).orElseThrow();
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
                var answerMd = switch (answerType) {
                    case GCP -> gcpApi.send(prompt);
                    case CLAUDE -> claudeApi.send(prompt);
                    case OPEN_AI -> openAiApi.send(prompt);
                    case GRAMMAR -> openAiGrammarApi.send(prompt);
                };
                var answerHtml = formatConverter.markdownToHtml(answerMd);
                updateAnswer(interactionId, answerType, answer ->
                        answer.withAnswerMd(answerMd).withAnswerHtml(answerHtml).withState(SUCCESS), callback);
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

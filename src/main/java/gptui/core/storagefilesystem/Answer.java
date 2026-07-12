package gptui.core.storagefilesystem;

import static gptui.core.util.LogUtils.shorten;

public record Answer(AnswerType answerType, String prompt,
                     String answerMd, String answerHtml, AnswerState answerState, String responseId,
                     String modelId, String modelParams, String finishReason, Integer inputTokens,
                     Integer outputTokens, Integer totalTokens) {

    public Answer withPrompt(String prompt) {
        return new Answer(answerType, prompt, answerMd, answerHtml, answerState, responseId,
                modelId, modelParams, finishReason, inputTokens, outputTokens, totalTokens);
    }

    public Answer withAnswerMd(String answerMd) {
        return new Answer(answerType, prompt, answerMd, answerHtml, answerState, responseId,
                modelId, modelParams, finishReason, inputTokens, outputTokens, totalTokens);
    }

    public Answer withAnswerHtml(String answerHtml) {
        return new Answer(answerType, prompt, answerMd, answerHtml, answerState, responseId,
                modelId, modelParams, finishReason, inputTokens, outputTokens, totalTokens);
    }

    public Answer withState(AnswerState answerState) {
        return new Answer(answerType, prompt, answerMd, answerHtml, answerState, responseId,
                modelId, modelParams, finishReason, inputTokens, outputTokens, totalTokens);
    }

    public Answer withResponseId(String responseId) {
        return new Answer(answerType, prompt, answerMd, answerHtml, answerState, responseId,
                modelId, modelParams, finishReason, inputTokens, outputTokens, totalTokens);
    }

    public Answer withModelInfo(String modelId, String modelParams, String finishReason, Integer inputTokens,
                                 Integer outputTokens, Integer totalTokens) {
        return new Answer(answerType, prompt, answerMd, answerHtml, answerState, responseId,
                modelId, modelParams, finishReason, inputTokens, outputTokens, totalTokens);
    }

    public String toShortString() {
        return withPrompt(shorten(prompt))
                .withAnswerMd(shorten(answerMd))
                .withAnswerHtml(shorten(answerHtml))
                .toString();
    }
}

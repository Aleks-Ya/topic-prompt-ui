package topicpromptui.core.storagefilesystem;

import java.util.List;

import static topicpromptui.core.util.LogUtils.shorten;

public record Answer(AnswerType answerType, String prompt,
                     String answerMd, String answerHtml, AnswerState answerState, String responseId,
                     String modelId, String effortLevel, String finishReason, Integer inputTokens,
                     Integer outputTokens, Integer totalTokens, List<String> toolCalls, String systemPrompt) {

    // Convenience constructor keeping the existing 12-arg call sites unchanged; toolCalls (the MCP
    // tool-call display lines) defaults to null. Stored JSON without the field also deserializes to null.
    public Answer(AnswerType answerType, String prompt, String answerMd, String answerHtml, AnswerState answerState,
                  String responseId, String modelId, String effortLevel, String finishReason, Integer inputTokens,
                  Integer outputTokens, Integer totalTokens) {
        this(answerType, prompt, answerMd, answerHtml, answerState, responseId, modelId, effortLevel, finishReason,
                inputTokens, outputTokens, totalTokens, null, null);
    }

    // Convenience constructor keeping the existing 13-arg call sites unchanged; systemPrompt defaults
    // to null. Stored JSON without the field also deserializes to null.
    public Answer(AnswerType answerType, String prompt, String answerMd, String answerHtml, AnswerState answerState,
                  String responseId, String modelId, String effortLevel, String finishReason, Integer inputTokens,
                  Integer outputTokens, Integer totalTokens, List<String> toolCalls) {
        this(answerType, prompt, answerMd, answerHtml, answerState, responseId, modelId, effortLevel, finishReason,
                inputTokens, outputTokens, totalTokens, toolCalls, null);
    }

    public Answer withPrompt(String prompt) {
        return new Answer(answerType, prompt, answerMd, answerHtml, answerState, responseId,
                modelId, effortLevel, finishReason, inputTokens, outputTokens, totalTokens, toolCalls, systemPrompt);
    }

    public Answer withAnswerMd(String answerMd) {
        return new Answer(answerType, prompt, answerMd, answerHtml, answerState, responseId,
                modelId, effortLevel, finishReason, inputTokens, outputTokens, totalTokens, toolCalls, systemPrompt);
    }

    public Answer withAnswerHtml(String answerHtml) {
        return new Answer(answerType, prompt, answerMd, answerHtml, answerState, responseId,
                modelId, effortLevel, finishReason, inputTokens, outputTokens, totalTokens, toolCalls, systemPrompt);
    }

    public Answer withState(AnswerState answerState) {
        return new Answer(answerType, prompt, answerMd, answerHtml, answerState, responseId,
                modelId, effortLevel, finishReason, inputTokens, outputTokens, totalTokens, toolCalls, systemPrompt);
    }

    public Answer withResponseId(String responseId) {
        return new Answer(answerType, prompt, answerMd, answerHtml, answerState, responseId,
                modelId, effortLevel, finishReason, inputTokens, outputTokens, totalTokens, toolCalls, systemPrompt);
    }

    public Answer withModelInfo(String modelId, String effortLevel, String finishReason, Integer inputTokens,
                                 Integer outputTokens, Integer totalTokens) {
        return new Answer(answerType, prompt, answerMd, answerHtml, answerState, responseId,
                modelId, effortLevel, finishReason, inputTokens, outputTokens, totalTokens, toolCalls, systemPrompt);
    }

    public Answer withToolCalls(List<String> toolCalls) {
        return new Answer(answerType, prompt, answerMd, answerHtml, answerState, responseId,
                modelId, effortLevel, finishReason, inputTokens, outputTokens, totalTokens, toolCalls, systemPrompt);
    }

    public Answer withSystemPrompt(String systemPrompt) {
        return new Answer(answerType, prompt, answerMd, answerHtml, answerState, responseId,
                modelId, effortLevel, finishReason, inputTokens, outputTokens, totalTokens, toolCalls, systemPrompt);
    }

    public String toShortString() {
        return withPrompt(shorten(prompt))
                .withAnswerMd(shorten(answerMd))
                .withAnswerHtml(shorten(answerHtml))
                .withSystemPrompt(shorten(systemPrompt))
                .toString();
    }
}

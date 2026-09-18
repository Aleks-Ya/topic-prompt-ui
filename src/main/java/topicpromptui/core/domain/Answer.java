package topicpromptui.core.domain;

import java.util.List;

import static topicpromptui.core.util.LogUtils.shorten;

public record Answer(AnswerType answerType, String prompt,
                     String answerMd, String answerHtml, AnswerState answerState, String responseId,
                     String modelId, String effortLevel, String finishReason, Integer inputTokens,
                     Integer outputTokens, Integer totalTokens, List<String> toolCalls, String systemPrompt,
                     List<Citation> citations) {

    // Convenience constructor keeping the existing 12-arg call sites unchanged; toolCalls (the MCP
    // tool-call display lines) defaults to null, as do systemPrompt and citations. Stored JSON
    // without those fields also deserializes them to null.
    public Answer(AnswerType answerType, String prompt, String answerMd, String answerHtml, AnswerState answerState,
                  String responseId, String modelId, String effortLevel, String finishReason, Integer inputTokens,
                  Integer outputTokens, Integer totalTokens) {
        this(answerType, prompt, answerMd, answerHtml, answerState, responseId, modelId, effortLevel, finishReason,
                inputTokens, outputTokens, totalTokens, null, null, null);
    }

    // Convenience constructor keeping the existing 13-arg call sites unchanged; systemPrompt and
    // citations default to null. Stored JSON without those fields also deserializes them to null.
    public Answer(AnswerType answerType, String prompt, String answerMd, String answerHtml, AnswerState answerState,
                  String responseId, String modelId, String effortLevel, String finishReason, Integer inputTokens,
                  Integer outputTokens, Integer totalTokens, List<String> toolCalls) {
        this(answerType, prompt, answerMd, answerHtml, answerState, responseId, modelId, effortLevel, finishReason,
                inputTokens, outputTokens, totalTokens, toolCalls, null, null);
    }

    public Answer withPrompt(String prompt) {
        return new Answer(answerType, prompt, answerMd, answerHtml, answerState, responseId,
                modelId, effortLevel, finishReason, inputTokens, outputTokens, totalTokens, toolCalls, systemPrompt,
                citations);
    }

    public Answer withAnswerMd(String answerMd) {
        return new Answer(answerType, prompt, answerMd, answerHtml, answerState, responseId,
                modelId, effortLevel, finishReason, inputTokens, outputTokens, totalTokens, toolCalls, systemPrompt,
                citations);
    }

    public Answer withAnswerHtml(String answerHtml) {
        return new Answer(answerType, prompt, answerMd, answerHtml, answerState, responseId,
                modelId, effortLevel, finishReason, inputTokens, outputTokens, totalTokens, toolCalls, systemPrompt,
                citations);
    }

    public Answer withState(AnswerState answerState) {
        return new Answer(answerType, prompt, answerMd, answerHtml, answerState, responseId,
                modelId, effortLevel, finishReason, inputTokens, outputTokens, totalTokens, toolCalls, systemPrompt,
                citations);
    }

    public Answer withResponseId(String responseId) {
        return new Answer(answerType, prompt, answerMd, answerHtml, answerState, responseId,
                modelId, effortLevel, finishReason, inputTokens, outputTokens, totalTokens, toolCalls, systemPrompt,
                citations);
    }

    public Answer withModelInfo(String modelId, String effortLevel, String finishReason, Integer inputTokens,
                                 Integer outputTokens, Integer totalTokens) {
        return new Answer(answerType, prompt, answerMd, answerHtml, answerState, responseId,
                modelId, effortLevel, finishReason, inputTokens, outputTokens, totalTokens, toolCalls, systemPrompt,
                citations);
    }

    public Answer withToolCalls(List<String> toolCalls) {
        return new Answer(answerType, prompt, answerMd, answerHtml, answerState, responseId,
                modelId, effortLevel, finishReason, inputTokens, outputTokens, totalTokens, toolCalls, systemPrompt,
                citations);
    }

    public Answer withCitations(List<Citation> citations) {
        return new Answer(answerType, prompt, answerMd, answerHtml, answerState, responseId,
                modelId, effortLevel, finishReason, inputTokens, outputTokens, totalTokens, toolCalls, systemPrompt,
                citations);
    }

    public Answer withSystemPrompt(String systemPrompt) {
        return new Answer(answerType, prompt, answerMd, answerHtml, answerState, responseId,
                modelId, effortLevel, finishReason, inputTokens, outputTokens, totalTokens, toolCalls, systemPrompt,
                citations);
    }

    public String toShortString() {
        return withPrompt(shorten(prompt))
                .withAnswerMd(shorten(answerMd))
                .withAnswerHtml(shorten(answerHtml))
                .withSystemPrompt(shorten(systemPrompt))
                .toString();
    }
}

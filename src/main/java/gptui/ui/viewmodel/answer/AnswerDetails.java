package gptui.ui.viewmodel.answer;

import gptui.core.storagefilesystem.AnswerType;

public record AnswerDetails(AnswerType answerType, String modelId, String effortLevel, String finishReason,
                             Integer inputTokens, Integer outputTokens, Integer totalTokens, String prompt) {
}

package topicpromptui.ui.viewmodel.answer;

import topicpromptui.core.storagefilesystem.AnswerType;

public record AnswerDetails(AnswerType answerType, String modelId, String effortLevel, String finishReason,
                             Integer inputTokens, Integer outputTokens, Integer totalTokens, String prompt) {
}

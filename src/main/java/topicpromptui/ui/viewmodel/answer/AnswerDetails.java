package topicpromptui.ui.viewmodel.answer;

import topicpromptui.core.storagefilesystem.AnswerType;

import java.util.List;

public record AnswerDetails(AnswerType answerType, String modelId, String effortLevel, String finishReason,
                             Integer inputTokens, Integer outputTokens, Integer totalTokens, String prompt,
                             List<String> toolCalls) {
}

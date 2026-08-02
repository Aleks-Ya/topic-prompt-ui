package topicpromptui.ui.viewmodel.answer;

import topicpromptui.core.storagefilesystem.AnswerType;
import topicpromptui.core.storagefilesystem.InteractionId;

import java.util.List;

public record AnswerDetails(InteractionId interactionId, AnswerType answerType, String modelId, String effortLevel,
                             String finishReason, Integer inputTokens, Integer outputTokens, Integer totalTokens,
                             String prompt, String systemPrompt, List<String> toolCalls) {
}

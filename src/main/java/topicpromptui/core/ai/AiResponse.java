package topicpromptui.core.ai;

import java.util.List;

public record AiResponse(String text, String responseId, String modelId, String effortLevel, String finishReason,
                         Integer inputTokens, Integer outputTokens, Integer totalTokens, List<String> toolCalls) {

    // Convenience constructor for providers that never call tools (GCP) and test mocks; keeps the
    // existing 8-arg call sites unchanged. toolCalls is a pre-formatted line per MCP tool call.
    public AiResponse(String text, String responseId, String modelId, String effortLevel, String finishReason,
                      Integer inputTokens, Integer outputTokens, Integer totalTokens) {
        this(text, responseId, modelId, effortLevel, finishReason, inputTokens, outputTokens, totalTokens, List.of());
    }
}

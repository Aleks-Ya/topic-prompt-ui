package gptui.core.ai;

public record AiResponse(String text, String responseId, String modelId, String effortLevel, String finishReason,
                         Integer inputTokens, Integer outputTokens, Integer totalTokens) {
}

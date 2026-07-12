package gptui.core.ai;

public record AiResponse(String text, String responseId, String modelId, String modelParams,
                         Integer inputTokens, Integer outputTokens, Integer totalTokens) {
}

package gptui.core.ai.gcp;

import java.util.List;

record ResponseBody(List<Candidate> candidates, String responseId, UsageMetadata usageMetadata) {
    record Candidate(Content content, FinishReason finishReason) {
    }

    @SuppressWarnings("unused")
    enum FinishReason {
        FINISH_REASON_UNSPECIFIED,
        STOP,
        MAX_TOKENS,
        SAFETY,
        RECITATION,
        OTHER
    }

    record UsageMetadata(Integer promptTokenCount, Integer candidatesTokenCount, Integer totalTokenCount) {
    }
}

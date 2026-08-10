package topicpromptui.core.ai.gcp;

import java.util.List;

record ResponseBody(List<Candidate> candidates, String responseId, UsageMetadata usageMetadata) {
    record Candidate(Content content, FinishReason finishReason, GroundingMetadata groundingMetadata) {
    }

    // Present only on grounded candidates. The rest of the payload (chunks, supports, rendered
    // content) is deliberately unmapped - only the queries are surfaced.
    record GroundingMetadata(List<String> webSearchQueries) {
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

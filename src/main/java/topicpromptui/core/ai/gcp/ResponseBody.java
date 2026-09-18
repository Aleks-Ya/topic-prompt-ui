package topicpromptui.core.ai.gcp;

import java.util.List;

record ResponseBody(List<Candidate> candidates, String responseId, UsageMetadata usageMetadata) {
    record Candidate(Content content, FinishReason finishReason, GroundingMetadata groundingMetadata) {
    }

    // Present only on grounded candidates. Supports and rendered content stay unmapped; only the
    // queries and the cited sources are surfaced.
    record GroundingMetadata(List<String> webSearchQueries, List<GroundingChunk> groundingChunks) {
    }

    // uri is a vertexaisearch.cloud.google.com redirect and title is usually a bare domain; both
    // are stored as Gemini returns them. Non-web chunk kinds leave web null.
    record GroundingChunk(Web web) {
        record Web(String uri, String title) {
        }
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

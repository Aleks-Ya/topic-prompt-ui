package topicpromptui.core.ai.claude;

/**
 * A single Claude Messages API SSE event payload. Only the fields the app reads are mapped;
 * unknown event types carry nulls and are ignored.
 */
record StreamEvent(String type, MessageStart message, Delta delta, Usage usage) {
    record MessageStart(String id, Usage usage) {
    }

    record Delta(String type, String text, String stop_reason) {
    }

    record Usage(Integer input_tokens, Integer output_tokens) {
    }
}

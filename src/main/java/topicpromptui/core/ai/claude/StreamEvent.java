package topicpromptui.core.ai.claude;

/**
 * A single Claude Messages API SSE event payload. Only the fields the app reads are mapped;
 * unknown event types carry nulls and are ignored.
 */
record StreamEvent(String type, Integer index, ContentBlock content_block, MessageStart message, Delta delta,
                   Usage usage) {
    record MessageStart(String id, Usage usage) {
    }

    // Carried on content_block_start; for MCP the type is "mcp_tool_use" with a name + server_name.
    record ContentBlock(String type, String name, String server_name) {
    }

    // partial_json accumulates the tool input on input_json_delta events.
    record Delta(String type, String text, String partial_json, String stop_reason) {
    }

    record Usage(Integer input_tokens, Integer output_tokens) {
    }
}

package topicpromptui.core.ai.claude;

import com.google.gson.JsonElement;

/**
 * A single Claude Messages API SSE event payload. Only the fields the app reads are mapped;
 * unknown event types carry nulls and are ignored.
 */
record StreamEvent(String type, Integer index, ContentBlock content_block, MessageStart message, Delta delta,
                   Usage usage) {
    record MessageStart(String id, Usage usage) {
    }

    // Carried on content_block_start; for MCP the type is "mcp_tool_use" with a name + server_name.
    // content holds the tool results the sources are read from, and stays a raw JsonElement because
    // its shape depends on the block type: an array of web_search_result objects on
    // web_search_tool_result, a single web_fetch_result object on web_fetch_tool_result.
    record ContentBlock(String type, String name, String server_name, JsonElement content) {
    }

    // partial_json accumulates the tool input on input_json_delta events.
    record Delta(String type, String text, String partial_json, String stop_reason) {
    }

    record Usage(Integer input_tokens, Integer output_tokens) {
    }
}

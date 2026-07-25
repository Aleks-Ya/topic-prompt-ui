package topicpromptui.core.ai.claude;

import com.google.gson.annotations.SerializedName;

import java.util.List;

record RequestBody(String model, Integer max_tokens, List<Message> messages, OutputConfig output_config,
                   Boolean stream, List<McpServer> mcp_servers, List<McpToolset> tools) {
}

record Message(String role, String content) {
}

// Remote MCP server declaration for Anthropic's server-side MCP connector (beta mcp-client-2025-11-20).
// authorization_token is forwarded by Anthropic to the MCP server as "Authorization: Bearer <token>".
record McpServer(String type, String name, String url, String authorization_token) {
}

record McpToolset(String type, String mcp_server_name) {
}

record OutputConfig(Effort effort) {
}

enum Effort {
    @SerializedName("low")
    LOW,

    @SerializedName("medium")
    MEDIUM,

    @SerializedName("high")
    HIGH,

    @SerializedName("xhigh")
    XHIGH,

    @SerializedName("max")
    MAX
}

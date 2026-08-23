package topicpromptui.core.ai.xai;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

record RequestBody(String model, String instructions, List<InputItem> input, Reasoning reasoning, Boolean stream,
                   List<Tool> tools) {
}

record InputItem(String role, String content) {
}

// Hosted remote-MCP tool. xAI mirrors the OpenAI Responses tool schema: headers is forwarded to the
// MCP server (e.g. {"Authorization": "Bearer <token>"}); require_approval "never" auto-runs calls.
record Tool(String type, String server_label, String server_url, Map<String, String> headers,
            String require_approval) {
}

record Reasoning(ReasoningEffort effort) {
}

// xAI accepts only these four levels (default "high"); unlike OpenAI it rejects "none"/"minimal".
enum ReasoningEffort {
    @SerializedName("low")
    LOW,

    @SerializedName("medium")
    MEDIUM,

    @SerializedName("high")
    HIGH,

    @SerializedName("xhigh")
    XHIGH
}

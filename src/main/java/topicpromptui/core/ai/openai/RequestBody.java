package topicpromptui.core.ai.openai;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

record RequestBody(String model, String instructions, List<InputItem> input, Reasoning reasoning, Boolean stream,
                   List<Tool> tools) {
}

record InputItem(String role, String content) {
}

// Hosted remote-MCP tool for the OpenAI Responses API. headers is forwarded by OpenAI to the MCP
// server (e.g. {"Authorization": "Bearer <token>"}); require_approval "never" auto-runs tool calls.
record Tool(String type, String server_label, String server_url, Map<String, String> headers,
            String require_approval) {
}

record Reasoning(ReasoningEffort effort) {
}

enum ReasoningEffort {
    @SerializedName("none")
    NONE,

    @SerializedName("minimal")
    MINIMAL,

    @SerializedName("low")
    LOW,

    @SerializedName("medium")
    MEDIUM,

    @SerializedName("high")
    HIGH,

    @SerializedName("xhigh")
    XHIGH
}

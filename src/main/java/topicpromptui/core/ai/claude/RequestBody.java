package topicpromptui.core.ai.claude;

import com.google.gson.annotations.SerializedName;

import java.util.List;

record RequestBody(String model, Integer max_tokens, List<Message> messages, OutputConfig output_config,
                   Boolean stream) {
}

record Message(String role, String content) {
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

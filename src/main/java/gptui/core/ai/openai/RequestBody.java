package gptui.core.ai.openai;

import com.google.gson.annotations.SerializedName;

import java.util.List;

record RequestBody(String model, List<InputItem> input, Reasoning reasoning, Boolean stream) {
}

record InputItem(String role, String content) {
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

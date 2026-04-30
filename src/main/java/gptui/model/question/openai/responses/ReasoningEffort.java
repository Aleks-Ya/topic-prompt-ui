package gptui.model.question.openai.responses;

import com.google.gson.annotations.SerializedName;

public enum ReasoningEffort {
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

package gptui.core.ai.gcp;

import com.google.gson.annotations.SerializedName;

import java.util.List;

record RequestBody(List<Content> contents, GenerationConfig generationConfig) {
}

record Content(List<Part> parts, String role) {
}

record Part(String text) {
}

record GenerationConfig(Integer candidateCount, ThinkingConfig thinkingConfig) {
}

record ThinkingConfig(ThinkingLevel thinkingLevel) {
}

enum ThinkingLevel {
    @SerializedName("low")
    LOW,

    @SerializedName("medium")
    MEDIUM,

    @SerializedName("high")
    HIGH
}
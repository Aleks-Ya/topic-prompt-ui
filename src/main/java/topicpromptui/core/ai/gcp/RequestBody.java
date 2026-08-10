package topicpromptui.core.ai.gcp;

import com.google.gson.annotations.SerializedName;

import java.util.List;

record RequestBody(List<Content> contents, Content systemInstruction, GenerationConfig generationConfig,
                   List<Tool> tools) {
}

// Gemini identifies a built-in tool by field name rather than a "type" discriminator, each with an
// empty object as its config, so Gson's null-skipping is what leaves exactly one field per entry.
record Tool(GoogleSearch googleSearch, UrlContext urlContext) {
}

record GoogleSearch() {
}

record UrlContext() {
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
package gptui.core.ai.gcp;

import java.util.List;

record RequestBody(List<Content> contents, GenerationConfig generationConfig) {
}

record Content(List<Part> parts, String role) {
}

record Part(String text) {
}

record GenerationConfig(Integer candidateCount) {
}
package gptui.core.ai.gcp;

import java.math.BigDecimal;
import java.util.List;

record RequestBody(List<Content> contents, GenerationConfig generationConfig) {
}

record Content(List<Part> parts, String role) {
}

record Part(String text) {
}

record GenerationConfig(BigDecimal temperature, Integer candidateCount) {
}
package gptui.core.ai.openai;

import java.util.List;

record ResponseBody(String id, String model, List<Outputs> output, Error error, Usage usage) {
    public record Content(String text) {
    }

    public record Outputs(List<Content> content, String status) {
    }

    record Error(String message, String type, String param, String code) {
    }

    record Usage(Integer input_tokens, Integer output_tokens, Integer total_tokens) {
    }
}

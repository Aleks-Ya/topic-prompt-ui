package gptui.model.question.openai.responses;

import java.util.List;

record ResponseBody(String model, List<Outputs> output, Error error) {
    public record Content(String text) {}

    public record Outputs(List<Content> content, String status) {
    }

    record Error(String message, String type, String param, String code) {
    }
}

package gptui.model.question.openai.responses;

record RequestBody(String model, String input, Reasoning reasoning) {
}

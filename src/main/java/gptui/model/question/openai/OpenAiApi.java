package gptui.model.question.openai;

public interface OpenAiApi {
    String send(String content, Integer temperature);
}

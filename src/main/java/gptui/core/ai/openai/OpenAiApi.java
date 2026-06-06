package gptui.core.ai.openai;

public interface OpenAiApi {
    String send(String content, Integer temperature);
}

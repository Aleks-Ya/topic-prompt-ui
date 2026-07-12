package gptui.core.ai;

public record ConversationTurn(Speaker speaker, String content) {
    public enum Speaker {
        USER,
        MODEL
    }
}

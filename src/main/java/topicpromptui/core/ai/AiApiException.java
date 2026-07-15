package topicpromptui.core.ai;

public class AiApiException extends RuntimeException {
    public AiApiException(String message) {
        super(message);
    }

    public AiApiException(Throwable cause) {
        super(cause);
    }
}

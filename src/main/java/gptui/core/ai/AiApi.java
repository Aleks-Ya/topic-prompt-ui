package gptui.core.ai;

import java.util.List;
import java.util.function.Consumer;

public interface AiApi {
    default AiResponse send(String content) {
        return send(List.of(new ConversationTurn(ConversationTurn.Speaker.USER, content)));
    }

    default AiResponse send(String content, Consumer<String> onTextDelta) {
        return send(List.of(new ConversationTurn(ConversationTurn.Speaker.USER, content)), onTextDelta);
    }

    default AiResponse send(List<ConversationTurn> turns) {
        return send(turns, delta -> {
        });
    }

    /**
     * Sends the conversation and blocks until the full answer is assembled.
     * {@code onTextDelta} is invoked on the calling thread for each streamed text fragment.
     */
    AiResponse send(List<ConversationTurn> turns, Consumer<String> onTextDelta);
}

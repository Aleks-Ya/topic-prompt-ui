package topicpromptui.core.ai;

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

    default AiResponse send(List<ConversationTurn> turns, Consumer<String> onTextDelta) {
        return send(null, turns, onTextDelta);
    }

    /**
     * Sends the conversation and blocks until the full answer is assembled.
     * {@code systemPrompt} sets the provider's system/instructions field (Claude {@code system},
     * OpenAI {@code instructions}, GCP {@code systemInstruction}); {@code null} omits it so the
     * provider falls back to its own default. {@code onTextDelta} is invoked on the calling thread
     * for each streamed text fragment.
     */
    AiResponse send(String systemPrompt, List<ConversationTurn> turns, Consumer<String> onTextDelta);
}

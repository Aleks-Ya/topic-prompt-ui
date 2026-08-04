package topicpromptui.core.ai;

import java.util.List;
import java.util.function.Consumer;

public interface AiApi {
    /**
     * Sends the conversation and blocks until the full answer is assembled.
     *
     * @param systemPrompt sets the provider's system/instructions field (Claude {@code system},
     *                     OpenAI {@code instructions}, GCP {@code systemInstruction}); {@code null}
     *                     omits it so the provider falls back to its own default
     * @param turns        the conversation history to send
     * @param onTextDelta  invoked on the calling thread for each streamed text fragment
     * @return the assembled response
     */
    AiResponse send(String systemPrompt, List<ConversationTurn> turns, Consumer<String> onTextDelta);
}

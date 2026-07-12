package gptui.core.ai;

import java.util.List;

public interface AiApi {
    default AiResponse send(String content) {
        return send(List.of(new ConversationTurn(ConversationTurn.Speaker.USER, content)));
    }

    AiResponse send(List<ConversationTurn> turns);
}

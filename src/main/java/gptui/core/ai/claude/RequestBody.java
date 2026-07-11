package gptui.core.ai.claude;

import java.util.List;

record RequestBody(String model, Integer max_tokens, List<Message> messages) {
}

record Message(String role, String content) {
}

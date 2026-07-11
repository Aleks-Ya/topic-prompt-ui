package gptui.core.ai.claude;

import java.math.BigDecimal;
import java.util.List;

record RequestBody(String model, Integer max_tokens, List<Message> messages, BigDecimal temperature) {
}

record Message(String role, String content) {
}

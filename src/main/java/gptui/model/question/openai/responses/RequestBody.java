package gptui.model.question.openai.responses;

import java.math.BigDecimal;

record RequestBody(String model, String input, BigDecimal temperature) {
}

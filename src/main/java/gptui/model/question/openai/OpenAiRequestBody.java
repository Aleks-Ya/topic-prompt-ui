package gptui.model.question.openai;

import java.math.BigDecimal;

record OpenAiRequestBody(String model, String input, BigDecimal temperature) {
}

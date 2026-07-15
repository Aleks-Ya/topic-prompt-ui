package topicpromptui.core.ai.openai;

/**
 * A single OpenAI Responses API SSE event payload. {@code delta} is set on
 * {@code response.output_text.delta} events; {@code response} is the full final body on
 * {@code response.completed}/{@code response.failed}/{@code response.incomplete} events.
 */
record StreamEvent(String type, String delta, ResponseBody response) {
}

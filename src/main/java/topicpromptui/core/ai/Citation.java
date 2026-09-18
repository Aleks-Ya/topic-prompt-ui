package topicpromptui.core.ai;

/**
 * One web source a provider cited. {@code title} is whatever the provider supplied - Gemini
 * returns a bare domain, Claude and the Responses-API providers a page title - and may be null.
 */
public record Citation(String url, String title) {
}

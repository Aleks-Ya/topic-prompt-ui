package topicpromptui.core.domain;

/**
 * A web source cited by a provider, as persisted on an {@link Answer}. Mirrors
 * {@code topicpromptui.core.ai.Citation}, which stays in the AI layer because {@code core.ai}
 * has no dependency on {@code core.domain}.
 */
public record Citation(String url, String title) {
}

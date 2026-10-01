package topicpromptui.core.ai;

import java.util.HashSet;
import java.util.List;

/**
 * Providers repeat the same source once per sentence it supports, so every impl funnels its
 * collected citations through {@link #dedup(List)} before building the {@link AiResponse}.
 */
public final class Citations {
    private Citations() {
    }

    /**
     * Keeps the first occurrence of each URL, in encounter order; entries with a blank URL are
     * dropped since they point at nothing.
     */
    public static List<Citation> dedup(List<Citation> citations) {
        var seen = new HashSet<String>();
        return citations.stream()
                .filter(citation -> citation.url() != null && !citation.url().isBlank())
                .filter(citation -> seen.add(citation.url()))
                .toList();
    }
}

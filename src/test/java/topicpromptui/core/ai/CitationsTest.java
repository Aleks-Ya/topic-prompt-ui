package topicpromptui.core.ai;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CitationsTest {
    @Test
    void dedupKeepsFirstOccurrenceOfEachUrlInOrder() {
        var citations = Citations.dedup(List.of(
                new Citation("https://a.org", "A"),
                new Citation("https://b.org", "B"),
                new Citation("https://a.org", "A again"),
                new Citation("https://c.org", null)));
        assertThat(citations).containsExactly(
                new Citation("https://a.org", "A"),
                new Citation("https://b.org", "B"),
                new Citation("https://c.org", null));
    }

    @Test
    void dedupDropsEntriesWithoutUrl() {
        assertThat(Citations.dedup(List.of(new Citation(null, "A"), new Citation("  ", "B")))).isEmpty();
    }
}

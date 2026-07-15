package topicpromptui.core.ai;

import topicpromptui.core.ai.SseParser.SseEvent;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SseParserTest {

    @Test
    void eventAndDataLines() {
        var events = SseParser.parse(Stream.of(
                "event: message_start",
                "data: {\"a\":1}",
                "",
                "event: message_stop",
                "data: {}",
                ""));
        assertThat(events).containsExactly(
                new SseEvent("message_start", "{\"a\":1}"),
                new SseEvent("message_stop", "{}"));
    }

    @Test
    void dataOnlyEvents() {
        var events = SseParser.parse(Stream.of("data: {\"b\":2}", ""));
        assertThat(events).containsExactly(new SseEvent(null, "{\"b\":2}"));
    }

    @Test
    void multipleDataLinesAreJoinedWithNewline() {
        var events = SseParser.parse(Stream.of("data: line1", "data: line2", ""));
        assertThat(events).containsExactly(new SseEvent(null, "line1\nline2"));
    }

    @Test
    void commentsAndDoneSentinelAreSkipped() {
        var events = SseParser.parse(Stream.of(
                ": keep-alive",
                "data: {\"c\":3}",
                "",
                "data: [DONE]",
                ""));
        assertThat(events).containsExactly(new SseEvent(null, "{\"c\":3}"));
    }

    @Test
    void trailingEventWithoutBlankLineIsDispatched() {
        var events = SseParser.parse(Stream.of("event: end", "data: {\"d\":4}"));
        assertThat(events).containsExactly(new SseEvent("end", "{\"d\":4}"));
    }

    @Test
    void valueWithoutSpaceAfterColon() {
        var events = SseParser.parse(Stream.of("event:end", "data:x", ""));
        assertThat(events).containsExactly(new SseEvent("end", "x"));
    }

    @Test
    void joinLines() {
        assertThat(SseParser.joinLines(Stream.of("a", "b"))).isEqualTo("a\nb");
    }
}

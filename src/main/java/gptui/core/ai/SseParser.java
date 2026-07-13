package gptui.core.ai;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Minimal Server-Sent Events framing parser shared by the three AiApi implementations.
 * Folds "event:"/"data:" lines into {@link SseEvent}s; event interpretation stays per-provider.
 */
public final class SseParser {
    private SseParser() {
    }

    public record SseEvent(String event, String data) {
    }

    public static void forEachEvent(Stream<String> lines, Consumer<SseEvent> handler) {
        var eventName = new StringBuilder();
        var data = new StringBuilder();
        Runnable dispatch = () -> {
            if (!data.isEmpty()) {
                var payload = data.toString();
                if (!"[DONE]".equals(payload)) {
                    handler.accept(new SseEvent(eventName.isEmpty() ? null : eventName.toString(), payload));
                }
            }
            eventName.setLength(0);
            data.setLength(0);
        };
        for (var iterator = lines.iterator(); iterator.hasNext(); ) {
            var line = iterator.next();
            if (line.isEmpty()) {
                dispatch.run();
            } else if (line.startsWith(":")) {
                // comment line, e.g. OpenAI keep-alive
            } else if (line.startsWith("event:")) {
                eventName.setLength(0);
                eventName.append(stripFieldValue(line, "event:"));
            } else if (line.startsWith("data:")) {
                if (!data.isEmpty()) {
                    data.append('\n');
                }
                data.append(stripFieldValue(line, "data:"));
            }
            // other SSE fields (id:, retry:) are irrelevant for these providers
        }
        dispatch.run(); // trailing event without a final blank line
    }

    private static String stripFieldValue(String line, String field) {
        var value = line.substring(field.length());
        return value.startsWith(" ") ? value.substring(1) : value;
    }

    /** Collects the given lines back into a single string, for error bodies of non-200 responses. */
    public static String joinLines(Stream<String> lines) {
        return lines.collect(java.util.stream.Collectors.joining("\n"));
    }

    /** Convenience for tests. */
    public static List<SseEvent> parse(Stream<String> lines) {
        var events = new java.util.ArrayList<SseEvent>();
        forEachEvent(lines, events::add);
        return List.copyOf(events);
    }
}

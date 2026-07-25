package topicpromptui.core.ai;

/**
 * Formats a single MCP tool call into a compact display line shared by all providers, e.g.
 * {@code "context7 · get-library-docs {"context7CompatibleLibraryID":"/facebook/react"}"}.
 * These lines are surfaced verbatim in the answer info dialog.
 */
public final class ToolCalls {
    private ToolCalls() {
    }

    public static String line(String server, String name, String arguments) {
        var sb = new StringBuilder();
        if (server != null && !server.isBlank()) {
            sb.append(server).append(" · ");
        }
        sb.append(name == null || name.isBlank() ? "(unknown)" : name);
        if (arguments != null && !arguments.isBlank()) {
            sb.append(' ').append(arguments.strip());
        }
        return sb.toString();
    }
}

package topicpromptui.core.ai.openai;

import java.util.List;

record ResponseBody(String id, String model, List<Outputs> output, Error error, Usage usage) {
    public record Content(String text) {
    }

    // type distinguishes the assistant "message" output from the bookkeeping ones that appear when
    // tools are enabled ("mcp_list_tools", "mcp_call", "web_search_call", "reasoning").
    // name/server_label/arguments are populated on "mcp_call" only; "web_search_call" uses "action".
    public record Outputs(String type, List<Content> content, String status, String name, String server_label,
                          String arguments, Action action) {
    }

    // query is set for a "search" action, url for an "open_page" one.
    public record Action(String type, String query, String url) {
    }

    record Error(String message, String type, String param, String code) {
    }

    record Usage(Integer input_tokens, Integer output_tokens, Integer total_tokens) {
    }
}

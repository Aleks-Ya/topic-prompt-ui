package topicpromptui.core.ai.openai;

import java.util.List;

record ResponseBody(String id, String model, List<Outputs> output, Error error, Usage usage) {
    public record Content(String text) {
    }

    // type distinguishes the assistant "message" output from MCP bookkeeping outputs
    // ("mcp_list_tools", "mcp_call") and "reasoning" outputs that appear when tools are enabled.
    // name/server_label/arguments are populated on "mcp_call" outputs (the tool invocations).
    public record Outputs(String type, List<Content> content, String status, String name, String server_label,
                          String arguments) {
    }

    record Error(String message, String type, String param, String code) {
    }

    record Usage(Integer input_tokens, Integer output_tokens, Integer total_tokens) {
    }
}

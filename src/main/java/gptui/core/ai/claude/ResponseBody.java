package gptui.core.ai.claude;

import java.util.List;

record ResponseBody(String id, List<ContentBlock> content, String stop_reason, Usage usage) {
    record ContentBlock(String type, String text) {
    }

    record Usage(Integer input_tokens, Integer output_tokens) {
    }
}

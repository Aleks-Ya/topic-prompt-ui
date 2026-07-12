package gptui.core.ai.claude;

import java.util.List;

record ResponseBody(String id, List<ContentBlock> content, String stop_reason) {
    record ContentBlock(String type, String text) {
    }
}

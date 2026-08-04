package topicpromptui.core.ai.grader.graders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import topicpromptui.core.ai.AiResponse;
import topicpromptui.core.ai.grader.Grader;
import topicpromptui.core.ai.grader.Score;

import java.util.Locale;

public class ToolCallsContainGrader implements Grader {
    private static final Logger log = LoggerFactory.getLogger(ToolCallsContainGrader.class);

    private final String substring;

    public ToolCallsContainGrader(String substring) {
        this.substring = substring;
    }

    @Override
    public Score grade(AiResponse response) {
        var toolCalls = response.toolCalls();
        if (toolCalls == null || toolCalls.isEmpty()) {
            log.warn("Tool calls are empty: {}", toolCalls);
            return Score.MIN;
        }
        var needle = substring.toLowerCase(Locale.ROOT);
        if (toolCalls.stream().anyMatch(toolCall -> toolCall != null
                && toolCall.toLowerCase(Locale.ROOT).contains(needle))) {
            return Score.MAX;
        }
        log.warn("No tool call contains '{}': {}", substring, toolCalls);
        return Score.MIN;
    }
}

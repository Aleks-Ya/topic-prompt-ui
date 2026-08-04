package topicpromptui.core.ai.grader.graders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import topicpromptui.core.ai.AiResponse;
import topicpromptui.core.ai.grader.Grader;
import topicpromptui.core.ai.grader.Score;

public class EffortLevelGrader implements Grader {
    private static final Logger log = LoggerFactory.getLogger(EffortLevelGrader.class);
    private final String expected;

    public EffortLevelGrader(String expected) {
        this.expected = expected;
    }

    @Override
    public Score grade(AiResponse response) {
        var effortLevel = response.effortLevel();
        if (effortLevel.equals(expected)) {
            return Score.MAX;
        }
        log.warn("Effort level '{}' does not match expected '{}'", effortLevel, expected);
        return Score.MIN;
    }
}

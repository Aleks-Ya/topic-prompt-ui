package topicpromptui.core.ai.grader.graders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import topicpromptui.core.ai.AiResponse;
import topicpromptui.core.ai.grader.Grader;
import topicpromptui.core.ai.grader.Score;

public class FinishReasonGrader implements Grader {
    private static final Logger log = LoggerFactory.getLogger(FinishReasonGrader.class);
    private final String expected;

    public FinishReasonGrader(String expected) {
        this.expected = expected;
    }

    @Override
    public Score grade(AiResponse response) {
        var reason = response.finishReason();
        if (reason.equals(expected)) {
            return Score.MAX;
        }
        log.warn("Finish reason {} does not match expected {}", reason, expected);
        return Score.MIN;
    }
}

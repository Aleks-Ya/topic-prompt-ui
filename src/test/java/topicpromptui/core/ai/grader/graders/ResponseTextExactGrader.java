package topicpromptui.core.ai.grader.graders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import topicpromptui.core.ai.AiResponse;
import topicpromptui.core.ai.grader.Grader;
import topicpromptui.core.ai.grader.Score;

public class ResponseTextExactGrader implements Grader {
    private static final Logger log = LoggerFactory.getLogger(ResponseTextExactGrader.class);
    private final String expected;

    public ResponseTextExactGrader(String expected) {
        this.expected = expected;
    }

    @Override
    public Score grade(AiResponse response) {
        var text = response.text();
        if (text.equals(expected)) {
            return Score.MAX;
        }
        log.warn("Text '{}' does not match expected '{}'", text, expected);
        return Score.MIN;
    }
}

package topicpromptui.core.ai.grader.graders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import topicpromptui.core.ai.AiResponse;
import topicpromptui.core.ai.grader.Grader;
import topicpromptui.core.ai.grader.Score;

public class ResponseTextLengthGrader implements Grader {
    private static final Logger log = LoggerFactory.getLogger(ResponseTextLengthGrader.class);
    private final int min;
    private final int max;

    public ResponseTextLengthGrader(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public Score grade(AiResponse response) {
        var text = response.text();
        var length = text.length();
        if (length >= min && length <= max) {
            return Score.MAX;
        }
        log.warn("Length {} of text '{}' does not match expected length range [{}-{}]", length, text, min, max);
        return Score.MIN;
    }
}

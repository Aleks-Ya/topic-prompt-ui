package topicpromptui.core.ai.grader.graders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import topicpromptui.core.ai.AiResponse;
import topicpromptui.core.ai.grader.Grader;
import topicpromptui.core.ai.grader.Score;

public class ResponseTextNotBlankGrader implements Grader {
    private static final Logger log = LoggerFactory.getLogger(ResponseTextNotBlankGrader.class);

    @Override
    public Score grade(AiResponse response) {
        var text = response.text();
        if (text != null && !text.isBlank()) {
            return Score.MAX;
        }
        log.warn("Response text is blank: {}", text);
        return Score.MIN;
    }
}

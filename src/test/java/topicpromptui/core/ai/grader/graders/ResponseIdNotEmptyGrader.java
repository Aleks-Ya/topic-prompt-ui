package topicpromptui.core.ai.grader.graders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import topicpromptui.core.ai.AiResponse;
import topicpromptui.core.ai.grader.Grader;
import topicpromptui.core.ai.grader.Score;

public class ResponseIdNotEmptyGrader implements Grader {
    private static final Logger log = LoggerFactory.getLogger(ResponseIdNotEmptyGrader.class);

    @Override
    public Score grade(AiResponse response) {
        var responseId = response.responseId();
        if (responseId != null) {
            return Score.MAX;
        }
        log.warn("Response ID is null");
        return Score.MIN;
    }
}

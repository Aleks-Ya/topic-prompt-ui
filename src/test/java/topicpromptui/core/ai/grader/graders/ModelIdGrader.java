package topicpromptui.core.ai.grader.graders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import topicpromptui.core.ai.AiResponse;
import topicpromptui.core.ai.grader.Grader;
import topicpromptui.core.ai.grader.Score;

public class ModelIdGrader implements Grader {
    private static final Logger log = LoggerFactory.getLogger(ModelIdGrader.class);

    private final String expected;

    public ModelIdGrader(String expected) {
        this.expected = expected;
    }

    @Override
    public Score grade(AiResponse response) {
        var modelId = response.modelId();
        if (modelId.equals(expected)) {
            return Score.MAX;
        }
        log.warn("Model ID '{}' is not '{}'", modelId, expected);
        return Score.MIN;
    }
}

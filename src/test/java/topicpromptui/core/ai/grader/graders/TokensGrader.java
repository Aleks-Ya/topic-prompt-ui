package topicpromptui.core.ai.grader.graders;

import topicpromptui.core.ai.AiResponse;
import topicpromptui.core.ai.grader.Grader;
import topicpromptui.core.ai.grader.Score;

public class TokensGrader implements Grader {
    @Override
    public Score grade(AiResponse response) {
        if (response.inputTokens() > 0 && response.outputTokens() > 0 && response.totalTokens() > 0) {
            return Score.MAX;
        }
        return Score.MIN;
    }
}

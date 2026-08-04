package topicpromptui.core.ai.grader;

import topicpromptui.core.ai.AiResponse;

import java.util.Arrays;

import static topicpromptui.core.ai.grader.Score.MIN;

public interface Grader {
    Score grade(AiResponse response);

    static Score combine(AiResponse response, Grader... graders) {
        return Arrays.stream(graders).map(grader -> grader.grade(response)).reduce(Score::combine).orElse(MIN);
    }
}


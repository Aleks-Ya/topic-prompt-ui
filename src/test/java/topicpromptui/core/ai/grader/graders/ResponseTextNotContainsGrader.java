package topicpromptui.core.ai.grader.graders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import topicpromptui.core.ai.AiResponse;
import topicpromptui.core.ai.grader.Grader;
import topicpromptui.core.ai.grader.Score;

import java.util.Arrays;
import java.util.List;

public class ResponseTextNotContainsGrader implements Grader {
    private static final Logger log = LoggerFactory.getLogger(ResponseTextNotContainsGrader.class);
    private final List<String> forbidden;

    public ResponseTextNotContainsGrader(String... forbidden) {
        this.forbidden = Arrays.asList(forbidden);
    }

    public static ResponseTextNotContainsGrader noAsidePunctuation() {
        return new ResponseTextNotContainsGrader("—", "–", "--", "(", ")", "[", "]");
    }

    @Override
    public Score grade(AiResponse response) {
        var text = response.text();
        var found = forbidden.stream().filter(text::contains).toList();
        if (found.isEmpty()) {
            return Score.MAX;
        }
        log.warn("Text '{}' contains forbidden substrings '{}'", text, found);
        return Score.MIN;
    }
}

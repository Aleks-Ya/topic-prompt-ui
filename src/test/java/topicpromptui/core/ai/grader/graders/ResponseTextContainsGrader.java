package topicpromptui.core.ai.grader.graders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import topicpromptui.core.ai.AiResponse;
import topicpromptui.core.ai.grader.Grader;
import topicpromptui.core.ai.grader.Score;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Matches case-insensitively: capitalization is the model's whim rather than something the prompt can pin down
 * (asked for a bare fruit name, Gemini answers `Mango.` or `mango` where OpenAI answers `Mango`).
 */
public class ResponseTextContainsGrader implements Grader {
    private static final Logger log = LoggerFactory.getLogger(ResponseTextContainsGrader.class);
    private final List<String> expected;

    public ResponseTextContainsGrader(String... expected) {
        this.expected = Arrays.asList(expected);
    }

    @Override
    public Score grade(AiResponse response) {
        var text = response.text();
        var lowerCaseText = text.toLowerCase(Locale.ROOT);
        var missing = expected.stream()
                .filter(e -> !lowerCaseText.contains(e.toLowerCase(Locale.ROOT)))
                .toList();
        if (missing.isEmpty()) {
            return Score.MAX;
        }
        log.warn("Text '{}' does not contain expected substrings '{}'", text, missing);
        return Score.MIN;
    }
}

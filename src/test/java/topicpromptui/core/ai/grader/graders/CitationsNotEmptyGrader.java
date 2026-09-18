package topicpromptui.core.ai.grader.graders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import topicpromptui.core.ai.AiResponse;
import topicpromptui.core.ai.grader.Grader;
import topicpromptui.core.ai.grader.Score;

public class CitationsNotEmptyGrader implements Grader {
    private static final Logger log = LoggerFactory.getLogger(CitationsNotEmptyGrader.class);

    @Override
    public Score grade(AiResponse response) {
        var citations = response.citations();
        if (citations == null || citations.isEmpty()) {
            log.warn("Citations are empty: {}", citations);
            return Score.MIN;
        }
        var withoutUrl = citations.stream().filter(citation -> citation.url() == null || citation.url().isBlank())
                .toList();
        if (!withoutUrl.isEmpty()) {
            log.warn("Citations without a URL: {}", withoutUrl);
            return Score.MIN;
        }
        return Score.MAX;
    }
}

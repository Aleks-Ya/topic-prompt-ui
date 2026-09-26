package topicpromptui.core.ai.claude;

import topicpromptui.core.ai.AiApi;
import topicpromptui.ui.model.question.BaseMockApi;
import jakarta.inject.Singleton;

import java.time.Duration;

/**
 * Bound in {@code TestRootModule} even though no {@code AnswerType} slot selects Claude by default:
 * without it, selecting {@link topicpromptui.core.domain.AiProvider#CLAUDE} for a slot would silently
 * send every UI test to the real Claude API.
 */
@Singleton
public class MockClaudeApi extends BaseMockApi implements AiApi {

    @SuppressWarnings("UnusedReturnValue")
    public MockClaudeApi putFactResponse(String response, Duration timeout) {
        put("factually correct", null, response, timeout);
        return this;
    }

    @Override
    public MockClaudeApi clear() {
        super.clear();
        return this;
    }
}

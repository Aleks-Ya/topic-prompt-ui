package topicpromptui.core.ai.claude;

import topicpromptui.core.ai.AiApi;
import topicpromptui.ui.model.question.BaseMockApi;
import jakarta.inject.Singleton;

import java.time.Duration;

@Singleton
public class MockClaudeApi extends BaseMockApi implements AiApi {

    @SuppressWarnings("UnusedReturnValue")
    public MockClaudeApi putClaudeResponse(String response, Duration timeout) {
        put("I will ask you a question about", "a short response", response, timeout);
        return this;
    }

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

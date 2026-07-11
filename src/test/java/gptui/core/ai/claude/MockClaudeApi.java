package gptui.core.ai.claude;

import gptui.core.ai.AiApi;
import gptui.ui.model.question.BaseMockApi;
import jakarta.inject.Singleton;

import java.time.Duration;

@Singleton
public class MockClaudeApi extends BaseMockApi implements AiApi {

    @SuppressWarnings("UnusedReturnValue")
    public MockClaudeApi putLongResponse(String response, Duration timeout) {
        put("I will ask you a question about", "a short response", response, timeout);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public MockClaudeApi putFactResponse(String response, Duration timeout) {
        put("factually correct", null, response, timeout);
        return this;
    }

    public MockClaudeApi clear() {
        super.clear();
        return this;
    }
}

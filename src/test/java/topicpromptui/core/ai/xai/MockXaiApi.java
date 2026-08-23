package topicpromptui.core.ai.xai;

import topicpromptui.core.ai.AiApi;
import topicpromptui.ui.model.question.BaseMockApi;
import jakarta.inject.Singleton;

import java.time.Duration;

@Singleton
public class MockXaiApi extends BaseMockApi implements AiApi {

    @SuppressWarnings("UnusedReturnValue")
    public MockXaiApi putGrokResponse(String response, Duration timeout) {
        put("Do not repeat the question", "a short response", response, timeout);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public MockXaiApi putFactResponse(String response, Duration timeout) {
        put("factually correct", null, response, timeout);
        return this;
    }

    @Override
    public MockXaiApi clear() {
        super.clear();
        return this;
    }
}

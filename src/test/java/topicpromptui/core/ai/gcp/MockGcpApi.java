package topicpromptui.core.ai.gcp;

import topicpromptui.core.ai.AiApi;
import topicpromptui.ui.model.question.BaseMockApi;
import jakarta.inject.Singleton;

import java.time.Duration;

@Singleton
public class MockGcpApi extends BaseMockApi implements AiApi {

    @SuppressWarnings("UnusedReturnValue")
    public MockGcpApi putGcpResponse(String response, Duration timeout) {
        put("I will ask you a question about", null, response, timeout);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public MockGcpApi putFactResponse(String response, Duration timeout) {
        put("factually correct", null, response, timeout);
        return this;
    }

    @Override
    public MockGcpApi clear() {
        super.clear();
        return this;
    }
}

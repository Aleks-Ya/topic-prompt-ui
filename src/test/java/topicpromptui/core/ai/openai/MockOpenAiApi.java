package topicpromptui.core.ai.openai;

import topicpromptui.core.ai.AiApi;
import topicpromptui.ui.model.question.BaseMockApi;
import jakarta.inject.Singleton;

import java.time.Duration;

@Singleton
public class MockOpenAiApi extends BaseMockApi implements AiApi {

    public MockOpenAiApi putGrammarResponse(String response, Duration timeout) {
        put("has grammatical mistakes", null, response, timeout);
        return this;
    }

    public MockOpenAiApi putOpenAiResponse(String response, Duration timeout) {
        put("Do not repeat the question", null, response, timeout);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public MockOpenAiApi putFactResponse(String response, Duration timeout) {
        put("factually correct", null, response, timeout);
        return this;
    }

    @Override
    public MockOpenAiApi clear() {
        super.clear();
        return this;
    }
}

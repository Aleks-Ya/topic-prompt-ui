package gptui.model.question.openai;

import gptui.model.question.BaseMockApi;
import jakarta.inject.Singleton;

import java.time.Duration;

@Singleton
public class MockOpenAiApi extends BaseMockApi implements OpenAiApi {

    public MockOpenAiApi putGrammarResponse(String response, Duration timeout) {
        put("has grammatical mistakes", null, response, timeout);
        return this;
    }

    public MockOpenAiApi putShortResponse(String response, Duration timeout) {
        put("a short response", null, response, timeout);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public MockOpenAiApi putLongResponse(String response, Duration timeout) {
        put("I will ask you a question about", "a short response", response, timeout);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public MockOpenAiApi putFactResponse(String response, Duration timeout) {
        put("factually correct", null, response, timeout);
        return this;
    }

    public MockOpenAiApi clear() {
        super.clear();
        return this;
    }
}

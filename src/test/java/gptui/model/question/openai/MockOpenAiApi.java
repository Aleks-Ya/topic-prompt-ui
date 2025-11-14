package gptui.model.question.openai;

import gptui.model.question.gcp.GcpApi;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;

@Singleton
public class MockOpenAiApi implements OpenAiApi, GcpApi {
    private static final Logger log = LoggerFactory.getLogger(MockOpenAiApi.class);
    private final Map<RequestInfo, ResponseInfo> contentSubstringToResponseMap = new HashMap<>();
    private final List<String> sendHistory = new ArrayList<>();
    private final AtomicInteger receivedCounter = new AtomicInteger();

    @Override
    public String send(String content, Integer temperature) {
        sendHistory.add(content);
        if (Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Should not run in the JavaFX Application Thread");
        }
        var info = contentSubstringToResponseMap.entrySet().stream()
                .filter(entry -> {
                    var contains = entry.getKey().containsOpt
                            .map(value -> content.toLowerCase().contains(value.toLowerCase()))
                            .orElse(false);
                    var notContains = entry.getKey().notContainOpt
                            .map(value -> !content.toLowerCase().contains(value.toLowerCase()))
                            .orElse(true);
                    return contains && notContains;
                })
                .findFirst()
                .orElseThrow()
                .getValue();
        try {
            Thread.sleep(info.timeout().toMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        var newValue = receivedCounter.incrementAndGet();
        log.trace("receivedCounter was incremented: {}", newValue);
        return info.content();
    }

    public void waitUntilSent(int counter) {
        log.debug("Start waiting: receivedCounter=" + receivedCounter.get());
        await().timeout(Duration.ofSeconds(15)).until(() -> receivedCounter.get() >= counter);
        log.debug("Finished waiting: receivedCounter=" + receivedCounter.get());
    }

    public List<String> getSendHistory() {
        return sendHistory;
    }

    public MockOpenAiApi putGrammarResponse(String response, Duration timeout) {
        return put("has grammatical mistakes", null, response, timeout);
    }

    public MockOpenAiApi putShortResponse(String response, Duration timeout) {
        return put("a short response", null, response, timeout);
    }

    @SuppressWarnings("UnusedReturnValue")
    public MockOpenAiApi putLongResponse(String response, Duration timeout) {
        return put("I will ask you a question about", "a short response", response, timeout);
    }

    @SuppressWarnings("UnusedReturnValue")
    public MockOpenAiApi putGcpResponse(String response, Duration timeout) {
        return put("Answer question about", null, response, timeout);
    }

    @SuppressWarnings("UnusedReturnValue")
    public MockOpenAiApi putFactResponse(String response, Duration timeout) {
        return put("factually correct", null, response, timeout);
    }

    private MockOpenAiApi put(String containsSubstring, String notContainSubstring, String response, Duration timeout) {
        var requestInfo = new RequestInfo(Optional.ofNullable(containsSubstring), Optional.ofNullable(notContainSubstring));
        var responseInfo = new ResponseInfo(response, timeout);
        contentSubstringToResponseMap.put(requestInfo, responseInfo);
        return this;
    }

    public MockOpenAiApi clear() {
        receivedCounter.set(0);
        contentSubstringToResponseMap.clear();
        sendHistory.clear();
        return this;
    }

    record RequestInfo(Optional<String> containsOpt, Optional<String> notContainOpt) {
    }

    record ResponseInfo(String content, Duration timeout) {
    }
}

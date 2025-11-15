package gptui.model.question;

import gptui.model.question.gcp.GcpApi;
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

public abstract class BaseMockApi implements GcpApi {
    private static final Logger log = LoggerFactory.getLogger(BaseMockApi.class);
    protected final Map<RequestInfo, ResponseInfo> contentSubstringToResponseMap = new HashMap<>();
    protected final List<String> sendHistory = new ArrayList<>();
    protected final AtomicInteger receivedCounter = new AtomicInteger();

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

    protected void put(String containsSubstring, String notContainSubstring, String response, Duration timeout) {
        var requestInfo = new RequestInfo(Optional.ofNullable(containsSubstring), Optional.ofNullable(notContainSubstring));
        var responseInfo = new ResponseInfo(response, timeout);
        contentSubstringToResponseMap.put(requestInfo, responseInfo);
    }

    public BaseMockApi clear() {
        receivedCounter.set(0);
        contentSubstringToResponseMap.clear();
        sendHistory.clear();
        return this;
    }

    public record RequestInfo(Optional<String> containsOpt, Optional<String> notContainOpt) {
    }

    public record ResponseInfo(String content, Duration timeout) {
    }
}

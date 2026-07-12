package gptui.ui.model.question;

import gptui.core.ai.AiApi;
import gptui.core.ai.AiResponse;
import gptui.core.ai.ConversationTurn;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static org.awaitility.Awaitility.await;

public abstract class BaseMockApi implements AiApi {
    private static final Logger log = LoggerFactory.getLogger(BaseMockApi.class);
    protected final Map<RequestInfo, ResponseInfo> contentSubstringToResponseMap = new HashMap<>();
    protected final List<String> sendHistory = new ArrayList<>();
    protected final List<List<ConversationTurn>> turnsSendHistory = new ArrayList<>();
    protected final AtomicInteger receivedCounter = new AtomicInteger();

    @Override
    public AiResponse send(List<ConversationTurn> turns) {
        var content = turns.getLast().content();
        sendHistory.add(content);
        turnsSendHistory.add(turns);
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
                .orElseThrow(() -> new NoSuchElementException(format(
                        "Not found mock content: content='%s', mapKeys='%s'",
                        content, contentSubstringToResponseMap.keySet())))
                .getValue();
        try {
            Thread.sleep(info.timeout().toMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        var newValue = receivedCounter.incrementAndGet();
        log.trace("receivedCounter was incremented: {}", newValue);
        return new AiResponse(info.content(), null, null, null, null, null, null, null);
    }

    public void waitUntilSent(int counter) {
        log.debug("Start waiting: receivedCounter={}", receivedCounter.get());
        await().timeout(Duration.ofSeconds(15)).until(() -> receivedCounter.get() >= counter);
        log.debug("Finished waiting: receivedCounter={}", receivedCounter.get());
    }

    public List<String> getSendHistory() {
        return sendHistory;
    }

    public List<List<ConversationTurn>> getTurnsSendHistory() {
        return turnsSendHistory;
    }

    protected void put(String containsSubstring, String notContainSubstring, String response, Duration timeout) {
        var requestInfo = new RequestInfo(Optional.ofNullable(containsSubstring), Optional.ofNullable(notContainSubstring));
        var responseInfo = new ResponseInfo(response, timeout);
        contentSubstringToResponseMap.put(requestInfo, responseInfo);
    }

    // Follow-up requests send the raw user-entered text (no FreeMarker template wrapping),
    // so unlike putXxxResponse (which match a fixed template phrase), this matches arbitrary text.
    public BaseMockApi putResponse(String containsSubstring, String response, Duration timeout) {
        put(containsSubstring, null, response, timeout);
        return this;
    }

    public BaseMockApi clear() {
        receivedCounter.set(0);
        contentSubstringToResponseMap.clear();
        sendHistory.clear();
        turnsSendHistory.clear();
        return this;
    }

    public record RequestInfo(Optional<String> containsOpt, Optional<String> notContainOpt) {
    }

    public record ResponseInfo(String content, Duration timeout) {
    }
}

package topicpromptui.ui.model.question;

import topicpromptui.core.ai.AiApi;
import topicpromptui.core.ai.AiResponse;
import topicpromptui.core.ai.ConversationTurn;
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
import java.util.function.Consumer;

import static java.lang.String.format;
import static org.awaitility.Awaitility.await;

public abstract class BaseMockApi implements AiApi {
    private static final Logger log = LoggerFactory.getLogger(BaseMockApi.class);
    protected final Map<RequestInfo, ResponseInfo> contentSubstringToResponseMap = new HashMap<>();
    protected final List<String> sendHistory = new ArrayList<>();
    protected final List<List<ConversationTurn>> turnsSendHistory = new ArrayList<>();
    protected final List<String> systemPromptHistory = new ArrayList<>();
    protected final AtomicInteger receivedCounter = new AtomicInteger();

    // Thread.sleep here simulates provider latency and the per-chunk delay of a real streaming
    // response for tests (see putStreamingResponse), not a substitute for polling.
    @Override
    @SuppressWarnings("java:S2925")
    public AiResponse send(String systemPrompt, List<ConversationTurn> turns, Consumer<String> onTextDelta) {
        var content = turns.getLast().content();
        sendHistory.add(content);
        turnsSendHistory.add(turns);
        systemPromptHistory.add(systemPrompt);
        // The behavioral phrases that distinguish request types (question/definition/fact/grammar)
        // now live in the system prompt, so match against both it and the last user message.
        var matchTarget = (systemPrompt != null ? systemPrompt + "\n" : "") + content;
        if (Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Should not run in the JavaFX Application Thread");
        }
        var info = contentSubstringToResponseMap.entrySet().stream()
                .filter(entry -> {
                    var contains = entry.getKey().containsOpt
                            .map(value -> matchTarget.toLowerCase().contains(value.toLowerCase()))
                            .orElse(false);
                    var notContains = entry.getKey().notContainOpt
                            .map(value -> !matchTarget.toLowerCase().contains(value.toLowerCase()))
                            .orElse(true);
                    return contains && notContains;
                })
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(format(
                        "Not found mock content: matchTarget='%s', mapKeys='%s'",
                        matchTarget, contentSubstringToResponseMap.keySet())))
                .getValue();
        try {
            Thread.sleep(info.timeout().toMillis());
            for (var chunk : info.chunks()) {
                if (!info.perChunkDelay().isZero()) {
                    Thread.sleep(info.perChunkDelay().toMillis());
                }
                onTextDelta.accept(chunk);
            }
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

    /** The system prompt passed to each send (may contain nulls for legacy no-system calls). */
    public List<String> getSystemPromptHistory() {
        return systemPromptHistory;
    }

    protected void put(String containsSubstring, String notContainSubstring, String response, Duration timeout) {
        var requestInfo = new RequestInfo(Optional.ofNullable(containsSubstring), Optional.ofNullable(notContainSubstring));
        var responseInfo = new ResponseInfo(response, timeout, List.of(response), Duration.ZERO);
        contentSubstringToResponseMap.put(requestInfo, responseInfo);
    }

    // Follow-up requests send the raw user-entered text (no FreeMarker template wrapping),
    // so unlike putXxxResponse (which match a fixed template phrase), this matches arbitrary text.
    public BaseMockApi putResponse(String containsSubstring, String response, Duration timeout) {
        put(containsSubstring, null, response, timeout);
        return this;
    }

    /** The final response text is the concatenation of {@code chunks}, emitted one delta per chunk. */
    public BaseMockApi putStreamingResponse(String containsSubstring, List<String> chunks, Duration perChunkDelay) {
        var requestInfo = new RequestInfo(Optional.of(containsSubstring), Optional.empty());
        var responseInfo = new ResponseInfo(String.join("", chunks), Duration.ZERO, chunks, perChunkDelay);
        contentSubstringToResponseMap.put(requestInfo, responseInfo);
        return this;
    }

    public BaseMockApi clear() {
        receivedCounter.set(0);
        contentSubstringToResponseMap.clear();
        sendHistory.clear();
        turnsSendHistory.clear();
        systemPromptHistory.clear();
        return this;
    }

    public record RequestInfo(Optional<String> containsOpt, Optional<String> notContainOpt) {
    }

    public record ResponseInfo(String content, Duration timeout, List<String> chunks, Duration perChunkDelay) {
    }
}

package topicpromptui.core.ai.gcp;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import topicpromptui.core.ai.AiApi;
import topicpromptui.core.ai.ConversationTurn;
import topicpromptui.core.config.ConfigurationModule;
import topicpromptui.ui.model.question.prompt.PromptFactory;
import topicpromptui.ui.model.question.prompt.PromptModule;
import topicpromptui.ui.model.storage.StorageModule;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static topicpromptui.core.ai.AiModule.GCP_AI;
import static topicpromptui.core.ai.ConversationTurn.Speaker.MODEL;
import static topicpromptui.core.ai.ConversationTurn.Speaker.USER;
import static topicpromptui.core.storagefilesystem.AnswerType.GCP;
import static topicpromptui.core.storagefilesystem.InteractionType.DEFINITION;

class GcpApiIT {
    private static final Logger log = LoggerFactory.getLogger(GcpApiIT.class);
    private final Injector injector = Guice.createInjector(new GcpModule(), new ConfigurationModule(),
            new StorageModule(), new PromptModule());
    private final AiApi api = injector.getInstance(Key.get(AiApi.class, Names.named(GCP_AI)));
    private final PromptFactory promptFactory = injector.getInstance(PromptFactory.class);

    @Test
    void send() {
        var response = api.send("What is the last Java version?");
        log.info("Response text: {}", response.text());
        log.info("responseId: {}", response.responseId());
        log.info("modelId: {}", response.modelId());
        log.info("effortLevel: {}", response.effortLevel());
        log.info("finishReason: {}", response.finishReason());
        log.info("tokens: input={} output={} total={}", response.inputTokens(), response.outputTokens(), response.totalTokens());
        assertThat(response.text()).isNotBlank();
        assertThat(response.responseId()).isNotBlank();
        assertThat(response.modelId()).isNotBlank();
        assertThat(response.effortLevel()).isNotBlank();
        assertThat(response.finishReason()).isEqualTo("STOP");
        assertThat(response.inputTokens()).isPositive();
        assertThat(response.outputTokens()).isPositive();
        assertThat(response.totalTokens()).isPositive();
    }

    @Test
    void definition() {
        var system = promptFactory.getSystemPrompt(DEFINITION, "AWS S3", GCP).orElse(null);
        var prompt = promptFactory.getPrompt(DEFINITION, "AWS S3", "Bucket", GCP).orElseThrow();
        var response = api.send(system, List.of(new ConversationTurn(USER, prompt)), _ -> { });
        log.info("Response text: {}", response.text());
        log.info("responseId: {}", response.responseId());
        assertThat(response.text()).isNotBlank();
        assertThat(response.responseId()).isNotBlank();
    }

    @Test
    void sendMultiTurn() {
        var turns = List.of(
                new ConversationTurn(USER, "My favorite fruit is mango. Just acknowledge, don't say anything else."),
                new ConversationTurn(MODEL, "Got it."),
                new ConversationTurn(USER, "What fruit did I say was my favorite? Answer with just the fruit name."));
        var response = api.send(turns);
        log.info("Response text: {}", response.text());
        assertThat(response.text().toLowerCase()).contains("mango");
    }

    @Test
    void sendStreaming() {
        var deltas = new java.util.concurrent.CopyOnWriteArrayList<String>();
        var response = api.send("List the last 5 Java LTS versions with one sentence about each.", deltas::add);
        log.info("Deltas count: {}", deltas.size());
        assertThat(deltas).hasSizeGreaterThan(1);
        assertThat(String.join("", deltas)).isEqualTo(response.text());
        assertThat(response.responseId()).isNotBlank();
        assertThat(response.finishReason()).isEqualTo("STOP");
        assertThat(response.totalTokens()).isPositive();
    }

    @Test
    void error() {
        assertThatThrownBy(() -> api.send((String) null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("INVALID_ARGUMENT");
    }
}
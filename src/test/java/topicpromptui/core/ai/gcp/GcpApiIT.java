package topicpromptui.core.ai.gcp;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.junit.jupiter.api.Test;
import topicpromptui.core.ai.AiApi;
import topicpromptui.core.ai.ConversationTurn;
import topicpromptui.core.ai.grader.Grader;
import topicpromptui.core.ai.grader.Score;
import topicpromptui.core.ai.grader.graders.EffortLevelGrader;
import topicpromptui.core.ai.grader.graders.FinishReasonGrader;
import topicpromptui.core.ai.grader.graders.ModelIdGrader;
import topicpromptui.core.ai.grader.graders.ResponseIdNotEmptyGrader;
import topicpromptui.core.ai.grader.graders.ResponseTextExactGrader;
import topicpromptui.core.ai.grader.graders.ResponseTextNotBlankGrader;
import topicpromptui.core.ai.grader.graders.TokensGrader;
import topicpromptui.core.config.ConfigurationModule;
import topicpromptui.core.domain.AnswerType;
import topicpromptui.core.prompt.PromptFactory;
import topicpromptui.core.prompt.PromptModule;
import topicpromptui.ui.model.storage.StorageModule;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static topicpromptui.core.ai.AiModule.GCP_AI;
import static topicpromptui.core.ai.ConversationTurn.Speaker.MODEL;
import static topicpromptui.core.ai.ConversationTurn.Speaker.USER;
import static topicpromptui.core.ai.TestConsumers.NO_OP;
import static topicpromptui.core.domain.InteractionType.DEFINITION;

class GcpApiIT {
    private final Injector injector = Guice.createInjector(new GcpModule(), new ConfigurationModule(),
            new StorageModule(), new PromptModule());
    private final AiApi api = injector.getInstance(Key.get(AiApi.class, Names.named(GCP_AI)));
    private final PromptFactory promptFactory = injector.getInstance(PromptFactory.class);

    @Test
    void send() {
        var response = api.send(null, List.of(new ConversationTurn(USER, "What is the last Java version?")), NO_OP);
        assertThat(Grader.combine(response,
                new ResponseIdNotEmptyGrader(),
                new ModelIdGrader("gemini-3.1-pro-preview"),
                new ResponseTextNotBlankGrader(),
                new EffortLevelGrader("HIGH"),
                new FinishReasonGrader("STOP"),
                new TokensGrader()
        )).isEqualTo(Score.MAX);
    }

    @Test
    void definition() {
        var system = promptFactory.getSystemPrompt(DEFINITION, "AWS S3", AnswerType.GCP).orElseThrow();
        var prompt = promptFactory.getPrompt(DEFINITION, "Bucket", AnswerType.GCP).orElseThrow();
        var response = api.send(system, List.of(new ConversationTurn(USER, prompt)), NO_OP);
        assertThat(Grader.combine(response,
                new ResponseIdNotEmptyGrader(),
                new ModelIdGrader("gemini-3.1-pro-preview"),
                new ResponseTextNotBlankGrader(),
                new EffortLevelGrader("HIGH"),
                new FinishReasonGrader("STOP"),
                new TokensGrader()
        )).isEqualTo(Score.MAX);
    }

    @Test
    void sendMultiTurn() {
        var turns = List.of(
                new ConversationTurn(USER, "My favorite fruit is mango. Just acknowledge, don't say anything else."),
                new ConversationTurn(MODEL, "Got it."),
                new ConversationTurn(USER, "What fruit did I say was my favorite? Answer with just the fruit name."));
        var response = api.send(null, turns, NO_OP);
        assertThat(Grader.combine(response,
                new ResponseIdNotEmptyGrader(),
                new ModelIdGrader("gemini-3.1-pro-preview"),
                new ResponseTextExactGrader("Mango"),
                new EffortLevelGrader("HIGH"),
                new FinishReasonGrader("STOP"),
                new TokensGrader()
        )).isEqualTo(Score.MAX);
    }

    @Test
    void sendStreaming() {
        var deltas = new CopyOnWriteArrayList<String>();
        var response = api.send(null, List.of(new ConversationTurn(USER,
                "List the last 5 Java LTS versions with one sentence about each.")), deltas::add);
        assertThat(deltas).hasSizeGreaterThan(1);
        assertThat(Grader.combine(response,
                new ResponseIdNotEmptyGrader(),
                new ModelIdGrader("gemini-3.1-pro-preview"),
                new ResponseTextExactGrader(String.join("", deltas)),
                new EffortLevelGrader("HIGH"),
                new FinishReasonGrader("STOP"),
                new TokensGrader()
        )).isEqualTo(Score.MAX);
    }

    @Test
    void error() {
        assertThatThrownBy(() -> api.send(null, List.of(new ConversationTurn(USER, null)), NO_OP))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("INVALID_ARGUMENT");
    }
}

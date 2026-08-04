package topicpromptui.core.ai.openai;

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
import topicpromptui.core.ai.grader.graders.ToolCallsContainGrader;
import topicpromptui.core.config.ConfigurationModule;
import topicpromptui.core.domain.AnswerType;
import topicpromptui.core.prompt.PromptFactory;
import topicpromptui.core.prompt.PromptModule;
import topicpromptui.ui.model.storage.StorageModule;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static topicpromptui.core.ai.AiModule.OPEN_AI;
import static topicpromptui.core.ai.ConversationTurn.Speaker.MODEL;
import static topicpromptui.core.ai.ConversationTurn.Speaker.USER;
import static topicpromptui.core.domain.InteractionType.DEFINITION;

class OpenAiApiIT {
    private final Injector injector = Guice.createInjector(new OpenAiModule(), new ConfigurationModule(),
            new StorageModule(), new PromptModule());
    private final AiApi api = injector.getInstance(Key.get(AiApi.class, Names.named(OPEN_AI)));
    private final PromptFactory promptFactory = injector.getInstance(PromptFactory.class);

    @Test
    void send() {
        var response = api.send("Who created Java?");
        assertThat(Grader.combine(response,
                new ResponseIdNotEmptyGrader(),
                new ModelIdGrader("gpt-5.6-sol"),
                new ResponseTextNotBlankGrader(),
                new EffortLevelGrader("XHIGH"),
                new FinishReasonGrader("completed"),
                new TokensGrader()
        )).isEqualTo(Score.MAX);
    }

    @Test
    void definitionOpenAi() {
        var system = promptFactory.getSystemPrompt(DEFINITION, "AWS S3", AnswerType.OPEN_AI).orElseThrow();
        var prompt = promptFactory.getPrompt(DEFINITION, "Bucket", AnswerType.OPEN_AI).orElseThrow();
        var response = api.send(system, List.of(new ConversationTurn(USER, prompt)), _ -> {
        });
        assertThat(Grader.combine(response,
                new ResponseIdNotEmptyGrader(),
                new ModelIdGrader("gpt-5.6-sol"),
                new ResponseTextNotBlankGrader(),
                new EffortLevelGrader("XHIGH"),
                new FinishReasonGrader("completed"),
                new TokensGrader()
        )).isEqualTo(Score.MAX);
    }

    @Test
    void sendMultiTurn() {
        var turns = List.of(
                new ConversationTurn(USER, "My favorite fruit is mango. Just acknowledge, don't say anything else."),
                new ConversationTurn(MODEL, "Got it."),
                new ConversationTurn(USER, "What fruit did I say was my favorite? Answer with just the fruit name."));
        var response = api.send(turns);
        assertThat(Grader.combine(response,
                new ResponseIdNotEmptyGrader(),
                new ModelIdGrader("gpt-5.6-sol"),
                new ResponseTextExactGrader("Mango"),
                new EffortLevelGrader("XHIGH"),
                new FinishReasonGrader("completed"),
                new TokensGrader()
        )).isEqualTo(Score.MAX);
    }

    @Test
    void sendStreaming() {
        var deltas = new CopyOnWriteArrayList<String>();
        var response = api.send("List the last 5 Java LTS versions with one sentence about each.", deltas::add);
        assertThat(deltas).hasSizeGreaterThan(1);
        assertThat(Grader.combine(response,
                new ResponseIdNotEmptyGrader(),
                new ModelIdGrader("gpt-5.6-sol"),
                new ResponseTextExactGrader(String.join("", deltas)),
                new EffortLevelGrader("XHIGH"),
                new FinishReasonGrader("completed"),
                new TokensGrader()
        )).isEqualTo(Score.MAX);
    }

    @Test
    void sendWithContext7Docs() {
        // Exercises the server-side Context7 MCP tool end to end (context7.api.key must be set): a green
        // run proves the extra mcp_list_tools/mcp_call outputs don't break parseResponse's selection.
        var response = api.send("Using the Context7 documentation, briefly explain what the Context7 MCP "
                + "server provides for developers. Consult the library docs before answering.");
        assertThat(Grader.combine(response,
                new ToolCallsContainGrader("Context7"),
                new ResponseIdNotEmptyGrader(),
                new ModelIdGrader("gpt-5.6-sol"),
                new ResponseTextNotBlankGrader(),
                new EffortLevelGrader("XHIGH"),
                new FinishReasonGrader("completed"),
                new TokensGrader()
        )).isEqualTo(Score.MAX);
    }

    @Test
    void error() {
        assertThatThrownBy(() -> api.send((String) null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("invalid_request_error");
    }

}
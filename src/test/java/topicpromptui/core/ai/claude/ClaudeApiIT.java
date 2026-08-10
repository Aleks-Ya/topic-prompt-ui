package topicpromptui.core.ai.claude;

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
import topicpromptui.core.ai.grader.graders.ResponseTextLengthGrader;
import topicpromptui.core.ai.grader.graders.ResponseTextNotContainsGrader;
import topicpromptui.core.ai.grader.graders.TokensGrader;
import topicpromptui.core.ai.grader.graders.ToolCallsContainGrader;
import topicpromptui.core.config.ProjectTemplatesConfigurationModule;
import topicpromptui.core.domain.AnswerType;
import topicpromptui.core.prompt.PromptFactory;
import topicpromptui.core.prompt.PromptModule;
import topicpromptui.ui.model.storage.StorageModule;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static topicpromptui.core.ai.AiModule.CLAUDE_AI;
import static topicpromptui.core.ai.ConversationTurn.Speaker.MODEL;
import static topicpromptui.core.ai.ConversationTurn.Speaker.USER;
import static topicpromptui.core.ai.TestConsumers.NO_OP;
import static topicpromptui.core.domain.InteractionType.DEFINITION;

class ClaudeApiIT {
    private final Injector injector = Guice.createInjector(new ClaudeModule(),
            new ProjectTemplatesConfigurationModule(), new StorageModule(), new PromptModule());
    private final AiApi api = injector.getInstance(Key.get(AiApi.class, Names.named(CLAUDE_AI)));
    private final PromptFactory promptFactory = injector.getInstance(PromptFactory.class);

    // Upper bound relaxed from 500 to 900: attaching the web tools makes Claude more expansive even
    // when it doesn't search (623/634 chars on two runs that had passed before).
    @Test
    void send() {
        var response = api.send(null, List.of(new ConversationTurn(USER, "Give me the name of the Java creator")), NO_OP);
        assertThat(Grader.combine(response,
                new ResponseIdNotEmptyGrader(),
                new ModelIdGrader("claude-opus-5"),
                new ResponseTextLengthGrader(10, 900),
                new EffortLevelGrader("XHIGH"),
                new FinishReasonGrader("end_turn"),
                new TokensGrader()
        )).isEqualTo(Score.MAX);
    }

    @Test
    void definitionClaude() {
        var system = promptFactory.getSystemPrompt(DEFINITION, "AWS S3", AnswerType.CLAUDE).orElseThrow();
        var prompt = promptFactory.getPrompt(DEFINITION, "Bucket", AnswerType.CLAUDE).orElseThrow();
        var response = api.send(system, List.of(new ConversationTurn(USER, prompt)), NO_OP);
        assertThat(Grader.combine(response,
                new ResponseIdNotEmptyGrader(),
                new ModelIdGrader("claude-opus-5"),
                new ResponseTextLengthGrader(100, 1000),
                new EffortLevelGrader("XHIGH"),
                new FinishReasonGrader("end_turn"),
                new TokensGrader(),
                ResponseTextNotContainsGrader.noAsidePunctuation()
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
                new ModelIdGrader("claude-opus-5"),
                new ResponseTextExactGrader("Mango"),
                new EffortLevelGrader("XHIGH"),
                new FinishReasonGrader("end_turn"),
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
                new ModelIdGrader("claude-opus-5"),
                new ResponseTextExactGrader(String.join("", deltas)),
                new EffortLevelGrader("XHIGH"),
                new FinishReasonGrader("end_turn"),
                new TokensGrader()
        )).isEqualTo(Score.MAX);
    }

    @Test
    void sendWithContext7Docs() {
        // Exercises the server-side Context7 MCP connector end to end (context7.api.key must be set):
        // a green run proves the MCP tool-use/tool-result blocks in the stream don't break assemble().
        var response = api.send(null, List.of(new ConversationTurn(USER, "Consult the Context7 library docs, then "
                + "say in a single sentence of at most 25 words what the Context7 MCP server provides for "
                + "developers. Output only that sentence.")), NO_OP);
        assertThat(Grader.combine(response,
                new ToolCallsContainGrader("Context7"),
                new ResponseIdNotEmptyGrader(),
                new ModelIdGrader("claude-opus-5"),
                new ResponseTextLengthGrader(20, 400),
                new EffortLevelGrader("XHIGH"),
                new TokensGrader()
        )).isEqualTo(Score.MAX);
    }

    @Test
    void sendWithWebSearch() {
        // An invalid tool type string compiles fine and is only rejected at request time, so this is
        // the gate for it - and for server_tool_use blocks not breaking assemble().
        var response = api.send(null, List.of(new ConversationTurn(USER, "Search the web for the current "
                + "stable version number of Node.js, then answer with just that version number.")), NO_OP);
        assertThat(Grader.combine(response,
                new ToolCallsContainGrader("web_search"),
                new ResponseIdNotEmptyGrader(),
                new ModelIdGrader("claude-opus-5"),
                new ResponseTextLengthGrader(1, 400),
                new EffortLevelGrader("XHIGH"),
                new TokensGrader()
        )).isEqualTo(Score.MAX);
    }

    @Test
    void sendWithWebFetch() {
        // The URL may be resolved by web_fetch or by a web_search, so this asserts only that some
        // tool touched it rather than pinning the tool name.
        var response = api.send(null, List.of(new ConversationTurn(USER, "Read https://openjdk.org/projects/jdk/25/ "
                + "and say in one sentence what that page is about.")), NO_OP);
        assertThat(Grader.combine(response,
                new ToolCallsContainGrader("openjdk.org"),
                new ResponseIdNotEmptyGrader(),
                new ModelIdGrader("claude-opus-5"),
                new ResponseTextLengthGrader(20, 500),
                new TokensGrader()
        )).isEqualTo(Score.MAX);
    }

    @Test
    void error() {
        var turns = List.of(new ConversationTurn(USER, null));
        assertThatThrownBy(() -> api.send(null, turns, NO_OP))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("invalid_request_error");
    }
}

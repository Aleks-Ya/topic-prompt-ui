package topicpromptui.core.ai.openai;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import topicpromptui.core.ai.grader.graders.TokensGrader;
import topicpromptui.core.config.ProjectTemplatesConfigurationModule;
import topicpromptui.core.domain.AnswerType;
import topicpromptui.core.prompt.PromptFactory;
import topicpromptui.core.prompt.PromptModule;
import topicpromptui.ui.model.storage.StorageModule;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static topicpromptui.core.ai.AiModule.OPEN_AI_GRAMMAR;
import static topicpromptui.core.ai.ConversationTurn.Speaker.USER;
import static topicpromptui.core.ai.TestConsumers.NO_OP;
import static topicpromptui.core.domain.AnswerType.GRAMMAR;
import static topicpromptui.core.domain.InteractionType.DEFINITION;
import static topicpromptui.core.domain.InteractionType.QUESTION;

class GrammarOpenAiApiIT {
    private final Injector injector = Guice.createInjector(new OpenAiModule(),
            new ProjectTemplatesConfigurationModule(), new StorageModule(), new PromptModule());
    private final AiApi grammarApi = injector.getInstance(Key.get(AiApi.class, Names.named(OPEN_AI_GRAMMAR)));
    private final PromptFactory promptFactory = injector.getInstance(PromptFactory.class);

    @Test
    void sendGrammar() {
        var response = grammarApi.send(null, List.of(new ConversationTurn(USER, "What is the last Java version?")),
                NO_OP);
        assertThat(Grader.combine(response,
                new ResponseIdNotEmptyGrader(),
                new ModelIdGrader("gpt-5.6-luna"),
                new ResponseTextLengthGrader(100, 1000),
                new EffortLevelGrader("MEDIUM"),
                new FinishReasonGrader("completed"),
                new TokensGrader()
        )).isEqualTo(Score.MAX);
    }

    @Test
    void definitionGrammarIncorrect() {
        var system = promptFactory.getSystemPrompt(DEFINITION, "Java", AnswerType.GRAMMAR).orElseThrow();
        var prompt = promptFactory.getPrompt(DEFINITION, "Garbaj collector", AnswerType.GRAMMAR).orElseThrow();
        var response = grammarApi.send(system, List.of(new ConversationTurn(USER, prompt)), NO_OP);
        assertThat(Grader.combine(response,
                new ResponseIdNotEmptyGrader(),
                new ModelIdGrader("gpt-5.6-luna"),
                new ResponseTextExactGrader("Garbage collector"),
                new EffortLevelGrader("MEDIUM"),
                new FinishReasonGrader("completed"),
                new TokensGrader()
        )).isEqualTo(Score.MAX);
    }

    @Test
    void definitionGrammarCorrect() {
        var system = promptFactory.getSystemPrompt(DEFINITION, "Java", AnswerType.GRAMMAR).orElseThrow();
        var prompt = promptFactory.getPrompt(DEFINITION, "Garbage collector", AnswerType.GRAMMAR).orElseThrow();
        var response = grammarApi.send(system, List.of(new ConversationTurn(USER, prompt)), NO_OP);
        assertThat(Grader.combine(response,
                new ResponseIdNotEmptyGrader(),
                new ModelIdGrader("gpt-5.6-luna"),
                new ResponseTextExactGrader("Correct"),
                new EffortLevelGrader("MEDIUM"),
                new FinishReasonGrader("completed"),
                new TokensGrader()
        )).isEqualTo(Score.MAX);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("questionGrammarCases")
    void questionGrammar(String caseName, String question, String expected) {
        var system = promptFactory.getSystemPrompt(QUESTION, "Java", GRAMMAR).orElseThrow();
        var prompt = promptFactory.getPrompt(QUESTION, question, GRAMMAR).orElseThrow();
        var response = grammarApi.send(system, List.of(new ConversationTurn(USER, prompt)), NO_OP);
        assertThat(Grader.combine(response,
                new ResponseIdNotEmptyGrader(),
                new ModelIdGrader("gpt-5.6-luna"),
                new ResponseTextExactGrader(expected),
                new EffortLevelGrader("MEDIUM"),
                new FinishReasonGrader("completed"),
                new TokensGrader()
        )).isEqualTo(Score.MAX);
    }

    static Stream<Arguments> questionGrammarCases() {
        return Stream.of(
                Arguments.of("incorrect", "What's latest Java version?", "What's the latest Java version?"),
                Arguments.of("correct", "What's the latest Java version?", "Correct"),
                Arguments.of("howTo", "How to prevent thread locks in an application?", "Correct"),
                Arguments.of("capitalLetters", "In which Java version was the Garbage Collector introduced?", "Correct")
        );
    }

    @Test
    void grammarBindingRunsWithoutTools() {
        // Guards against a future change flipping toolsEnabled on for both OpenAI bindings at once.
        var system = promptFactory.getSystemPrompt(QUESTION, "Java", GRAMMAR).orElseThrow();
        var prompt = promptFactory.getPrompt(QUESTION, "What is last version of Node.js?", GRAMMAR).orElseThrow();
        var response = grammarApi.send(system, List.of(new ConversationTurn(USER, prompt)), NO_OP);
        assertThat(response.toolCalls()).isEmpty();
    }

    @Test
    void errorGrammar() {
        var turns = List.of(new ConversationTurn(USER, null));
        assertThatThrownBy(() -> grammarApi.send(null, turns, NO_OP))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("invalid_request_error");
    }
}
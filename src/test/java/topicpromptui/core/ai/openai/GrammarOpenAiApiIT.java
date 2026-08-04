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
import topicpromptui.core.config.ConfigurationModule;
import topicpromptui.core.domain.AnswerType;
import topicpromptui.core.prompt.PromptFactory;
import topicpromptui.core.prompt.PromptModule;
import topicpromptui.ui.model.storage.StorageModule;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static topicpromptui.core.ai.AiModule.OPEN_AI_GRAMMAR;
import static topicpromptui.core.ai.ConversationTurn.Speaker.USER;
import static topicpromptui.core.domain.AnswerType.GRAMMAR;
import static topicpromptui.core.domain.InteractionType.DEFINITION;
import static topicpromptui.core.domain.InteractionType.QUESTION;

class GrammarOpenAiApiIT {
    private final Injector injector = Guice.createInjector(new OpenAiModule(), new ConfigurationModule(),
            new StorageModule(), new PromptModule());
    private final AiApi grammarApi = injector.getInstance(Key.get(AiApi.class, Names.named(OPEN_AI_GRAMMAR)));
    private final PromptFactory promptFactory = injector.getInstance(PromptFactory.class);

    @Test
    void sendGrammar() {
        var response = grammarApi.send("What is the last Java version?");
        assertThat(Grader.combine(response,
                new ResponseIdNotEmptyGrader(),
                new ModelIdGrader("gpt-5.6-luna"),
                new ResponseTextNotBlankGrader(),
                new EffortLevelGrader("MEDIUM"),
                new FinishReasonGrader("completed"),
                new TokensGrader()
        )).isEqualTo(Score.MAX);
    }

    @Test
    void definitionGrammarIncorrect() {
        var system = promptFactory.getSystemPrompt(DEFINITION, "Java", AnswerType.GRAMMAR).orElseThrow();
        var prompt = promptFactory.getPrompt(DEFINITION, "Garbaj collector", AnswerType.GRAMMAR).orElseThrow();
        var response = grammarApi.send(system, List.of(new ConversationTurn(USER, prompt)), _ -> {
        });
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
        var response = grammarApi.send(system, List.of(new ConversationTurn(USER, prompt)), _ -> {
        });
        assertThat(Grader.combine(response,
                new ResponseIdNotEmptyGrader(),
                new ModelIdGrader("gpt-5.6-luna"),
                new ResponseTextExactGrader("Correct"),
                new EffortLevelGrader("MEDIUM"),
                new FinishReasonGrader("completed"),
                new TokensGrader()
        )).isEqualTo(Score.MAX);
    }

    @Test
    void questionGrammarIncorrect() {
        var system = promptFactory.getSystemPrompt(QUESTION, "Java", GRAMMAR).orElseThrow();
        var prompt = promptFactory.getPrompt(QUESTION, "What's latest Java version?", GRAMMAR).orElseThrow();
        var response = grammarApi.send(system, List.of(new ConversationTurn(USER, prompt)), _ -> {
        });
        assertThat(Grader.combine(response,
                new ResponseIdNotEmptyGrader(),
                new ModelIdGrader("gpt-5.6-luna"),
                new ResponseTextExactGrader("What's **the** latest Java version?"),
                new EffortLevelGrader("MEDIUM"),
                new FinishReasonGrader("completed"),
                new TokensGrader()
        )).isEqualTo(Score.MAX);
    }

    @Test
    void questionGrammarCorrect() {
        var system = promptFactory.getSystemPrompt(QUESTION, "Java", GRAMMAR).orElseThrow();
        var prompt = promptFactory.getPrompt(QUESTION, "What's the latest Java version?", GRAMMAR).orElseThrow();
        var response = grammarApi.send(system, List.of(new ConversationTurn(USER, prompt)), _ -> {
        });
        assertThat(Grader.combine(response,
                new ResponseIdNotEmptyGrader(),
                new ModelIdGrader("gpt-5.6-luna"),
                new ResponseTextExactGrader("Correct"),
                new EffortLevelGrader("MEDIUM"),
                new FinishReasonGrader("completed"),
                new TokensGrader()
        )).isEqualTo(Score.MAX);
    }

    @Test
    void questionGrammarHowTo() {
        var system = promptFactory.getSystemPrompt(QUESTION, "Java", GRAMMAR).orElseThrow();
        var prompt = promptFactory.getPrompt(QUESTION, "How to prevent thread locks in an application?", GRAMMAR).orElseThrow();
        var response = grammarApi.send(system, List.of(new ConversationTurn(USER, prompt)), _ -> {
        });
        assertThat(Grader.combine(response,
                new ResponseIdNotEmptyGrader(),
                new ModelIdGrader("gpt-5.6-luna"),
                new ResponseTextExactGrader("Correct"),
                new EffortLevelGrader("MEDIUM"),
                new FinishReasonGrader("completed"),
                new TokensGrader()
        )).isEqualTo(Score.MAX);
    }

    @Test
    void questionGrammarCapitalLetters() {
        var system = promptFactory.getSystemPrompt(QUESTION, "Java", GRAMMAR).orElseThrow();
        var prompt = promptFactory.getPrompt(QUESTION, "In which Java version was the Garbage Collector introduced?", GRAMMAR).orElseThrow();
        var response = grammarApi.send(system, List.of(new ConversationTurn(USER, prompt)), _ -> {
        });
        assertThat(Grader.combine(response,
                new ResponseIdNotEmptyGrader(),
                new ModelIdGrader("gpt-5.6-luna"),
                new ResponseTextExactGrader("Correct"),
                new EffortLevelGrader("MEDIUM"),
                new FinishReasonGrader("completed"),
                new TokensGrader()
        )).isEqualTo(Score.MAX);
    }

    @Test
    void errorGrammar() {
        assertThatThrownBy(() -> grammarApi.send((String) null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("invalid_request_error");
    }
}
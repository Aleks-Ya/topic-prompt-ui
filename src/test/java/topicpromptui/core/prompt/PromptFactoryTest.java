package topicpromptui.core.prompt;

import topicpromptui.BaseTest;
import topicpromptui.core.config.ConfigModel;
import topicpromptui.core.domain.InteractionType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

import static topicpromptui.core.domain.AnswerType.CLAUDE;
import static topicpromptui.core.domain.AnswerType.GCP;
import static topicpromptui.core.domain.AnswerType.GRAMMAR;
import static topicpromptui.core.domain.AnswerType.OPEN_AI;
import static topicpromptui.core.domain.InteractionType.DEFINITION;
import static topicpromptui.core.domain.InteractionType.FACT;
import static topicpromptui.core.domain.InteractionType.QUESTION;
import static org.assertj.core.api.Assertions.assertThat;

class PromptFactoryTest extends BaseTest {
    private static final String GRAMMAR_SYSTEM = "Check if the sentence or phrase has grammatical mistakes.";
    private static final String GRAMMAR_SYSTEM_TOPIC = "in the context of the topic `Topic A`";
    private final PromptFactory factory = injector.getInstance(PromptFactory.class);
    private final ConfigModel configModel = injector.getInstance(ConfigModel.class);

    // --- user messages (topic + question only) ---

    @Test
    void questionUserMessage() {
        var grammar = """
                Sentence or phrase to check:
                ```
                Question A
                ```""";
        var question = """
                Question:
                ```
                Question A
                ```""";
        assertThat(factory.getPrompt(QUESTION, "Question A", GRAMMAR).orElseThrow()).contains(grammar);
        assertThat(factory.getPrompt(QUESTION, "Question A", OPEN_AI).orElseThrow()).contains(question);
        assertThat(factory.getPrompt(QUESTION, "Question A", CLAUDE).orElseThrow()).contains(question);
        assertThat(factory.getPrompt(QUESTION, "Question A", GCP).orElseThrow()).contains(question);
    }

    @Test
    void definitionUserMessage() {
        var definition = "Term: `Question A`";
        assertThat(factory.getPrompt(DEFINITION, "Question A", GRAMMAR).orElseThrow()).contains("""
                Sentence or phrase to check:
                ```
                Question A
                ```""");
        assertThat(factory.getPrompt(DEFINITION, "Question A", OPEN_AI).orElseThrow()).contains(definition);
        assertThat(factory.getPrompt(DEFINITION, "Question A", CLAUDE).orElseThrow()).contains(definition);
        assertThat(factory.getPrompt(DEFINITION, "Question A", GCP).orElseThrow()).contains(definition);
    }

    @Test
    void grammarUserMessage() {
        assertThat(factory.getPrompt(InteractionType.GRAMMAR, "Question A", GRAMMAR).orElseThrow()).contains("""
                Sentence or phrase to check:
                ```
                Question A
                ```""");
        assertThat(factory.getPrompt(InteractionType.GRAMMAR, "Question A", OPEN_AI)).isEmpty();
        assertThat(factory.getPrompt(InteractionType.GRAMMAR, "Question A", CLAUDE)).isEmpty();
        assertThat(factory.getPrompt(InteractionType.GRAMMAR, "Question A", GCP)).isEmpty();
    }

    @Test
    void factUserMessage() {
        var fact = """
                Statement to fact-check:
                ```
                Question A
                ```""";
        assertThat(factory.getPrompt(FACT, "Question A", GRAMMAR).orElseThrow()).contains("""
                Sentence or phrase to check:
                ```
                Question A
                ```""");
        assertThat(factory.getPrompt(FACT, "Question A", OPEN_AI).orElseThrow()).contains(fact);
        assertThat(factory.getPrompt(FACT, "Question A", CLAUDE).orElseThrow()).contains(fact);
        assertThat(factory.getPrompt(FACT, "Question A", GCP).orElseThrow()).contains(fact);
    }

    // --- system prompts (behavioral instructions + topic, no question) ---

    @Test
    void questionSystemPrompt() {
        var question = """
                You answer the user's questions in the context of the topic `Topic A`.
                Do not repeat the question in your answer.
                Avoid repeating the topic in your answer.
                Format your answer into Markdown.""";
        assertThat(factory.getSystemPrompt(QUESTION, "Topic A", GRAMMAR).orElseThrow()).contains(GRAMMAR_SYSTEM).contains(GRAMMAR_SYSTEM_TOPIC);
        assertThat(factory.getSystemPrompt(QUESTION, "Topic A", OPEN_AI).orElseThrow()).contains(question);
        assertThat(factory.getSystemPrompt(QUESTION, "Topic A", CLAUDE).orElseThrow()).contains(question);
        assertThat(factory.getSystemPrompt(QUESTION, "Topic A", GCP).orElseThrow()).contains(question);
    }

    @Test
    void definitionSystemPrompt() {
        var definition = """
                Provide a concise single-sentence definition of the given term in the context of the topic `Topic A`.
                Format your answer as `[the term] is/are`.
                Do not repeat the context in your answer if possible.""";
        assertThat(factory.getSystemPrompt(DEFINITION, "Topic A", GRAMMAR).orElseThrow()).contains(GRAMMAR_SYSTEM).contains(GRAMMAR_SYSTEM_TOPIC);
        assertThat(factory.getSystemPrompt(DEFINITION, "Topic A", OPEN_AI).orElseThrow()).contains(definition);
        assertThat(factory.getSystemPrompt(DEFINITION, "Topic A", CLAUDE).orElseThrow()).contains(definition);
        assertThat(factory.getSystemPrompt(DEFINITION, "Topic A", GCP).orElseThrow()).contains(definition);
    }

    @Test
    void grammarSystemPrompt() {
        assertThat(factory.getSystemPrompt(InteractionType.GRAMMAR, "Topic A", GRAMMAR).orElseThrow()).contains(GRAMMAR_SYSTEM).contains(GRAMMAR_SYSTEM_TOPIC);
        assertThat(factory.getSystemPrompt(InteractionType.GRAMMAR, "Topic A", OPEN_AI)).isEmpty();
        assertThat(factory.getSystemPrompt(InteractionType.GRAMMAR, "Topic A", CLAUDE)).isEmpty();
        assertThat(factory.getSystemPrompt(InteractionType.GRAMMAR, "Topic A", GCP)).isEmpty();
    }

    @Test
    void factSystemPrompt() {
        var fact = "Check whether the given statement is factually correct in the context of the topic `Topic A`.";
        assertThat(factory.getSystemPrompt(FACT, "Topic A", GRAMMAR).orElseThrow()).contains(GRAMMAR_SYSTEM).contains(GRAMMAR_SYSTEM_TOPIC);
        assertThat(factory.getSystemPrompt(FACT, "Topic A", OPEN_AI).orElseThrow()).contains(fact);
        assertThat(factory.getSystemPrompt(FACT, "Topic A", CLAUDE).orElseThrow()).contains(fact);
        assertThat(factory.getSystemPrompt(FACT, "Topic A", GCP).orElseThrow()).contains(fact);
    }

    @Test
    void userModifiesTemplate() throws IOException {
        var expDefaultPrompt = "Term: `Question A`";

        var templateFile = configModel.getAppDataPath().resolve("templates").resolve("definition.ftl");
        assertThat(factory.getPrompt(DEFINITION, "Question A", GCP).orElseThrow()).contains(expDefaultPrompt);

        Files.writeString(templateFile, "Answer ${question}");
        assertThat(factory.getPrompt(DEFINITION, "Question A", GCP).orElseThrow())
                .contains("Answer Question A");

        Files.delete(templateFile);
        assertThat(factory.getPrompt(DEFINITION, "Question A", GCP).orElseThrow()).contains(expDefaultPrompt);
    }
}

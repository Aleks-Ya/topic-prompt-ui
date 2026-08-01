package topicpromptui.ui.model.question.prompt;

import topicpromptui.BaseTest;
import topicpromptui.core.config.ConfigModel;
import topicpromptui.core.storagefilesystem.InteractionType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

import static topicpromptui.core.storagefilesystem.AnswerType.CLAUDE;
import static topicpromptui.core.storagefilesystem.AnswerType.GCP;
import static topicpromptui.core.storagefilesystem.AnswerType.GRAMMAR;
import static topicpromptui.core.storagefilesystem.AnswerType.OPEN_AI;
import static topicpromptui.core.storagefilesystem.InteractionType.DEFINITION;
import static topicpromptui.core.storagefilesystem.InteractionType.FACT;
import static topicpromptui.core.storagefilesystem.InteractionType.QUESTION;
import static org.assertj.core.api.Assertions.assertThat;

class PromptFactoryTest extends BaseTest {
    private static final String GRAMMAR_SYSTEM = "Check if the sentence or phrase has grammatical mistakes.";
    private final PromptFactory factory = injector.getInstance(PromptFactory.class);
    private final ConfigModel configModel = injector.getInstance(ConfigModel.class);

    // --- user messages (topic + question only) ---

    @Test
    void questionUserMessage() {
        var grammar = """
                Sentence or phrase to check, in the context of `Topic A`:
                ```
                Question A
                ```""";
        var question = """
                Topic: `Topic A`
                Question:
                ```
                Question A
                ```""";
        assertThat(factory.getPrompt(QUESTION, "Topic A", "Question A", GRAMMAR).orElseThrow()).contains(grammar);
        assertThat(factory.getPrompt(QUESTION, "Topic A", "Question A", OPEN_AI).orElseThrow()).contains(question);
        assertThat(factory.getPrompt(QUESTION, "Topic A", "Question A", CLAUDE).orElseThrow()).contains(question);
        assertThat(factory.getPrompt(QUESTION, "Topic A", "Question A", GCP).orElseThrow()).contains(question);
    }

    @Test
    void definitionUserMessage() {
        var definition = """
                Term: `Question A`
                Context: `Topic A`""";
        assertThat(factory.getPrompt(DEFINITION, "Topic A", "Question A", GRAMMAR).orElseThrow()).contains("""
                Text to grammar-check in the context of `Topic A`:
                `Question A`""");
        assertThat(factory.getPrompt(DEFINITION, "Topic A", "Question A", OPEN_AI).orElseThrow()).contains(definition);
        assertThat(factory.getPrompt(DEFINITION, "Topic A", "Question A", CLAUDE).orElseThrow()).contains(definition);
        assertThat(factory.getPrompt(DEFINITION, "Topic A", "Question A", GCP).orElseThrow()).contains(definition);
    }

    @Test
    void grammarUserMessage() {
        assertThat(factory.getPrompt(InteractionType.GRAMMAR, "Topic A", "Question A", GRAMMAR).orElseThrow()).contains("""
                Sentence or phrase to check, in the context of `Topic A`:
                ```
                Question A
                ```""");
        assertThat(factory.getPrompt(InteractionType.GRAMMAR, "Topic A", "Question A", OPEN_AI)).isEmpty();
        assertThat(factory.getPrompt(InteractionType.GRAMMAR, "Topic A", "Question A", CLAUDE)).isEmpty();
        assertThat(factory.getPrompt(InteractionType.GRAMMAR, "Topic A", "Question A", GCP)).isEmpty();
    }

    @Test
    void factUserMessage() {
        var fact = """
                Statement to fact-check in the context of `Topic A`:
                `Question A`""";
        assertThat(factory.getPrompt(FACT, "Topic A", "Question A", GRAMMAR).orElseThrow()).contains("""
                Sentence or phrase to check, in the context of `Topic A`:
                ```
                Question A
                ```""");
        assertThat(factory.getPrompt(FACT, "Topic A", "Question A", OPEN_AI).orElseThrow()).contains(fact);
        assertThat(factory.getPrompt(FACT, "Topic A", "Question A", CLAUDE).orElseThrow()).contains(fact);
        assertThat(factory.getPrompt(FACT, "Topic A", "Question A", GCP).orElseThrow()).contains(fact);
    }

    // --- system prompts (behavioral instructions, no topic/question) ---

    @Test
    void questionSystemPrompt() {
        var question = """
                Do not repeat the question in your answer.
                Avoid repeating the topic in your answer.
                Format your answer into Markdown.""";
        assertThat(factory.getSystemPrompt(QUESTION, GRAMMAR).orElseThrow()).contains(GRAMMAR_SYSTEM);
        assertThat(factory.getSystemPrompt(QUESTION, OPEN_AI).orElseThrow()).contains(question);
        assertThat(factory.getSystemPrompt(QUESTION, CLAUDE).orElseThrow()).contains(question);
        assertThat(factory.getSystemPrompt(QUESTION, GCP).orElseThrow()).contains(question);
    }

    @Test
    void definitionSystemPrompt() {
        var definition = """
                Provide a concise single-sentence definition of the given term in the context of the given topic.
                Format your answer as `<the term> is/are`.
                Do not repeat the context in your answer if possible.""";
        assertThat(factory.getSystemPrompt(DEFINITION, GRAMMAR).orElseThrow()).contains("""
                Check the grammar of the given text in the given context.
                If the text is correct, answer `Correct`.""");
        assertThat(factory.getSystemPrompt(DEFINITION, OPEN_AI).orElseThrow()).contains(definition);
        assertThat(factory.getSystemPrompt(DEFINITION, CLAUDE).orElseThrow()).contains(definition);
        assertThat(factory.getSystemPrompt(DEFINITION, GCP).orElseThrow()).contains(definition);
    }

    @Test
    void grammarSystemPrompt() {
        assertThat(factory.getSystemPrompt(InteractionType.GRAMMAR, GRAMMAR).orElseThrow()).contains(GRAMMAR_SYSTEM);
        assertThat(factory.getSystemPrompt(InteractionType.GRAMMAR, OPEN_AI)).isEmpty();
        assertThat(factory.getSystemPrompt(InteractionType.GRAMMAR, CLAUDE)).isEmpty();
        assertThat(factory.getSystemPrompt(InteractionType.GRAMMAR, GCP)).isEmpty();
    }

    @Test
    void factSystemPrompt() {
        var fact = "Check whether the given statement is factually correct in the context of the given topic.";
        assertThat(factory.getSystemPrompt(FACT, GRAMMAR).orElseThrow()).contains(GRAMMAR_SYSTEM);
        assertThat(factory.getSystemPrompt(FACT, OPEN_AI).orElseThrow()).contains(fact);
        assertThat(factory.getSystemPrompt(FACT, CLAUDE).orElseThrow()).contains(fact);
        assertThat(factory.getSystemPrompt(FACT, GCP).orElseThrow()).contains(fact);
    }

    @Test
    void userModifiesTemplate() throws IOException {
        var expDefaultPrompt = """
                Term: `Question A`
                Context: `Topic A`""";

        var templateFile = configModel.getAppDataPath().resolve("templates").resolve("definition-gcp.ftl");
        assertThat(factory.getPrompt(DEFINITION, "Topic A", "Question A", GCP).orElseThrow()).contains(expDefaultPrompt);

        Files.writeString(templateFile, "Answer ${question} about ${topic}");
        assertThat(factory.getPrompt(DEFINITION, "Topic A", "Question A", GCP).orElseThrow())
                .contains("Answer Question A about Topic A");

        Files.delete(templateFile);
        assertThat(factory.getPrompt(DEFINITION, "Topic A", "Question A", GCP).orElseThrow()).contains(expDefaultPrompt);
    }
}

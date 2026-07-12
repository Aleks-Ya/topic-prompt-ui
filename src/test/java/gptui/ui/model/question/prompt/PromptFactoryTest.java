package gptui.ui.model.question.prompt;

import gptui.BaseTest;
import gptui.ui.model.config.ConfigModel;
import gptui.core.storagefilesystem.InteractionType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

import static gptui.core.storagefilesystem.AnswerType.CLAUDE;
import static gptui.core.storagefilesystem.AnswerType.GCP;
import static gptui.core.storagefilesystem.AnswerType.GRAMMAR;
import static gptui.core.storagefilesystem.AnswerType.OPEN_AI;
import static gptui.core.storagefilesystem.InteractionType.DEFINITION;
import static gptui.core.storagefilesystem.InteractionType.FACT;
import static gptui.core.storagefilesystem.InteractionType.QUESTION;
import static org.assertj.core.api.Assertions.assertThat;

class PromptFactoryTest extends BaseTest {
    private final PromptFactory factory = injector.getInstance(PromptFactory.class);
    private final ConfigModel configModel = injector.getInstance(ConfigModel.class);

    @Test
    void question() {
        assertThat(factory.getPrompt(QUESTION, "Theme A", "Question A", GRAMMAR)).contains("""
                I will give you a sentence or phrase in the context of `Theme A`.
                Check if the sentence or phrase has grammatical mistakes.
                It is not a mistake if the sentence or phrase starts with `How to`.
                It is not a mistake if a term starts with a capital letter mid-sentence, since this can be intentional to denote a proper noun or a specific term (e.g. `Resume is a Slash Command in Claude Code that reopens a previous session.`).
                If the given sentence or phrase is correct, just answer `Correct`.
                If the sentence or phrase has mistakes, just answer with the correct sentence.
                Make the changed fragments bold.
                The sentence or phrase is:
                ```
                Question A
                ```""");
        assertThat(factory.getPrompt(QUESTION, "Theme A", "Question A", OPEN_AI)).contains("""
                I will ask you a question about `Theme A`.
                Do not repeat the question in your answer.
                Format your answer into Markdown.
                The question is:
                ```
                Question A
                ```""");
        assertThat(factory.getPrompt(QUESTION, "Theme A", "Question A", CLAUDE)).contains("""
                I will ask you a question about `Theme A`.
                Do not repeat the question in your answer.
                Format your answer into Markdown.
                The question is:
                ```
                Question A
                ```""");
        assertThat(factory.getPrompt(QUESTION, "Theme A", "Question A", GCP)).contains("""
                I will ask you a question about `Theme A`.
                Do not repeat the question in your answer.
                Format your answer into Markdown.
                The question is:
                ```
                Question A
                ```""");
    }

    @Test
    void definition() {
        assertThat(factory.getPrompt(DEFINITION, "Theme A", "Question A", GRAMMAR)).contains("""
                Check grammar of text `Question A` in the context of `Theme A`.
                If the text is correct, answer `Correct`.""");
        assertThat(factory.getPrompt(DEFINITION, "Theme A", "Question A", OPEN_AI)).contains("""
                Provide a concise single-sentence definition of `Question A` in the context of `Theme A`.
                Format your answer as `Question A is/are`.
                Do not repeat the context in your answer if possible.""");
        assertThat(factory.getPrompt(DEFINITION, "Theme A", "Question A", CLAUDE)).contains("""
                Provide a concise single-sentence definition of `Question A` in the context of `Theme A`.
                Format your answer as `Question A is/are`.
                Do not repeat the context in your answer if possible.""");
        assertThat(factory.getPrompt(DEFINITION, "Theme A", "Question A", GCP)).contains("""
                Provide a concise single-sentence definition of `Question A` in the context of `Theme A`.
                Format your answer as `Question A is/are`.
                Do not repeat the context in your answer if possible.""");
    }

    @Test
    void grammar() {
        assertThat(factory.getPrompt(InteractionType.GRAMMAR, "Theme A", "Question A", GRAMMAR)).contains("""
                I will give you a sentence or phrase in the context of `Theme A`.
                Check if the sentence or phrase has grammatical mistakes.
                It is not a mistake if the sentence or phrase starts with `How to`.
                It is not a mistake if a term starts with a capital letter mid-sentence, since this can be intentional to denote a proper noun or a specific term (e.g. `Resume is a Slash Command in Claude Code that reopens a previous session.`).
                If the given sentence or phrase is correct, just answer `Correct`.
                If the sentence or phrase has mistakes, just answer with the correct sentence.
                Make the changed fragments bold.
                The sentence or phrase is:
                ```
                Question A
                ```""");
        assertThat(factory.getPrompt(InteractionType.GRAMMAR, "Theme A", "Question A", OPEN_AI)).isEmpty();
        assertThat(factory.getPrompt(InteractionType.GRAMMAR, "Theme A", "Question A", CLAUDE)).isEmpty();
        assertThat(factory.getPrompt(InteractionType.GRAMMAR, "Theme A", "Question A", GCP)).isEmpty();
    }

    @Test
    void fact() {
        assertThat(factory.getPrompt(FACT, "Theme A", "Question A", GRAMMAR)).contains("""
                I will give you a sentence or phrase in the context of `Theme A`.
                Check if the sentence or phrase has grammatical mistakes.
                It is not a mistake if the sentence or phrase starts with `How to`.
                It is not a mistake if a term starts with a capital letter mid-sentence, since this can be intentional to denote a proper noun or a specific term (e.g. `Resume is a Slash Command in Claude Code that reopens a previous session.`).
                If the given sentence or phrase is correct, just answer `Correct`.
                If the sentence or phrase has mistakes, just answer with the correct sentence.
                Make the changed fragments bold.
                The sentence or phrase is:
                ```
                Question A
                ```""");
        assertThat(factory.getPrompt(FACT, "Theme A", "Question A", OPEN_AI))
                .contains("""
                        Check is this sentence factually correct in context of `Theme A`: `Question A`?
                        Format your answer into Markdown.""");
        assertThat(factory.getPrompt(FACT, "Theme A", "Question A", CLAUDE))
                .contains("""
                        Check is this sentence factually correct in context of `Theme A`: `Question A`?
                        Format your answer into Markdown.""");
        assertThat(factory.getPrompt(FACT, "Theme A", "Question A", GCP))
                .contains("""
                        Check is this sentence factually correct in context of `Theme A`: `Question A`?
                        Format your answer into Markdown.""");
    }

    @Test
    void userModifiesTemplate() throws IOException {
        var expDefaultPrompt = """
                Provide a concise single-sentence definition of `Question A` in the context of `Theme A`.
                Format your answer as `Question A is/are`.
                Do not repeat the context in your answer if possible.""";

        var templateFile = configModel.getAppDataPath().resolve("templates").resolve("definition-gcp.ftl");
        assertThat(factory.getPrompt(DEFINITION, "Theme A", "Question A", GCP)).contains(expDefaultPrompt);

        Files.writeString(templateFile, "Answer ${question} about ${theme}");
        assertThat(factory.getPrompt(DEFINITION, "Theme A", "Question A", GCP))
                .contains("Answer Question A about Theme A");

        Files.delete(templateFile);
        assertThat(factory.getPrompt(DEFINITION, "Theme A", "Question A", GCP)).contains(expDefaultPrompt);
    }
}
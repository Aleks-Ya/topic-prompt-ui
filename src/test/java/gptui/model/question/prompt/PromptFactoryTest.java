package gptui.model.question.prompt;

import gptui.BaseTest;
import gptui.model.config.ConfigModel;
import gptui.model.storage.InteractionType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

import static gptui.model.storage.AnswerType.GCP;
import static gptui.model.storage.AnswerType.GRAMMAR;
import static gptui.model.storage.AnswerType.LONG;
import static gptui.model.storage.AnswerType.SHORT;
import static gptui.model.storage.InteractionType.DEFINITION;
import static gptui.model.storage.InteractionType.FACT;
import static gptui.model.storage.InteractionType.QUESTION;
import static org.assertj.core.api.Assertions.assertThat;

class PromptFactoryTest extends BaseTest {
    private final PromptFactory factory = injector.getInstance(PromptFactory.class);
    private final ConfigModel configModel = injector.getInstance(ConfigModel.class);

    @Test
    void question() {
        assertThat(factory.getPrompt(QUESTION, "Theme A", "Question A", GRAMMAR)).contains("""
                I will give you a sentence or phrase. Check if the sentence or phrase has grammatical mistakes. It is not a mistake if the sentence or phrase starts with "How to". If the given sentence or phrase is correct, just answer "Correct". If the sentence or phrase has mistakes, just answer with correct sentence. The sentence or phrase is:
                ```
                Question A
                ```""");
        assertThat(factory.getPrompt(QUESTION, "Theme A", "Question A", SHORT)).contains("""
                I will ask you a question about "Theme A". You should answer with a short response. Do not repeat the question in your answer. Format your answer into Markdown. The question is:
                ```
                Question A
                ```""");
        assertThat(factory.getPrompt(QUESTION, "Theme A", "Question A", LONG)).contains("""
                I will ask you a question about "Theme A". Do not repeat the question in your answer. Format your answer into Markdown. The question is:
                ```
                Question A
                ```""");
        assertThat(factory.getPrompt(QUESTION, "Theme A", "Question A", GCP)).contains("""
                I will ask you a question about "Theme A". Do not repeat the question in your answer. The question is:
                ```
                Question A
                ```""");
    }

    @Test
    void definition() {
        assertThat(factory.getPrompt(DEFINITION, "Theme A", "Question A", GRAMMAR)).contains("""
                I will give you a phrase related to `Theme A`. Check if the phrase has grammatical mistakes. It is not a mistake if the phrase starts with "How to". The phrase is:
                ```
                Question A
                ```""");
        assertThat(factory.getPrompt(DEFINITION, "Theme A", "Question A", SHORT))
                .contains("Provide a single-sentence definition of `Question A` in the context of `Theme A`, as short as possible. " +
                        "Format your answer into Markdown.");
        assertThat(factory.getPrompt(DEFINITION, "Theme A", "Question A", LONG))
                .contains("Provide a detailed, single-sentence definition of `Question A` in the context of `Theme A`. " +
                        "Format your answer into Markdown.");
        assertThat(factory.getPrompt(DEFINITION, "Theme A", "Question A", GCP))
                .contains("Provide a single-sentence definition of `Question A` in the context of `Theme A`.");
    }

    @Test
    void grammar() {
        assertThat(factory.getPrompt(InteractionType.GRAMMAR, "Theme A", "Question A", GRAMMAR)).contains("""
                I will give you a sentence or phrase. Check if the sentence or phrase has grammatical mistakes. It is not a mistake if the sentence or phrase starts with "How to". If the given sentence or phrase is correct, just answer "Correct". If the sentence or phrase has mistakes, just answer with correct sentence. The sentence or phrase is:
                ```
                Question A
                ```""");
        assertThat(factory.getPrompt(InteractionType.GRAMMAR, "Theme A", "Question A", SHORT)).isEmpty();
        assertThat(factory.getPrompt(InteractionType.GRAMMAR, "Theme A", "Question A", LONG)).isEmpty();
        assertThat(factory.getPrompt(InteractionType.GRAMMAR, "Theme A", "Question A", GCP)).isEmpty();
    }

    @Test
    void fact() {
        assertThat(factory.getPrompt(FACT, "Theme A", "Question A", GRAMMAR))
                .contains("Check is this sentence factually correct in context of `Theme A`: `Question A`? " +
                        "Format your answer into Markdown.");
        assertThat(factory.getPrompt(InteractionType.GRAMMAR, "Theme A", "Question A", SHORT)).isEmpty();
        assertThat(factory.getPrompt(InteractionType.GRAMMAR, "Theme A", "Question A", LONG)).isEmpty();
        assertThat(factory.getPrompt(InteractionType.GRAMMAR, "Theme A", "Question A", GCP)).isEmpty();
    }

    @Test
    void userModifiesTemplate() throws IOException {
        var expDefaultPrompt = "Provide a single-sentence definition of `Question A` in the context of `Theme A`.";

        var templateFile = configModel.getAppDataPath().resolve("templates").resolve("definition-gcp.ftl");
        assertThat(factory.getPrompt(DEFINITION, "Theme A", "Question A", GCP)).contains(expDefaultPrompt);

        Files.writeString(templateFile, "Answer ${question} about ${theme}");
        assertThat(factory.getPrompt(DEFINITION, "Theme A", "Question A", GCP))
                .contains("Answer Question A about Theme A");

        Files.delete(templateFile);
        assertThat(factory.getPrompt(DEFINITION, "Theme A", "Question A", GCP)).contains(expDefaultPrompt);
    }
}
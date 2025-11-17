package gptui.model.question.prompt;

import gptui.model.storage.AnswerType;
import gptui.model.storage.InteractionType;
import jakarta.inject.Singleton;

import java.util.Optional;

import static gptui.util.ResourceUtils.resourceContent;
import static java.lang.String.format;

@Singleton
@SuppressWarnings("TextBlockMigration")
class PromptFactoryImpl implements PromptFactory {
    private final String questionShortTemplate = resourceContent(getClass(), "question-short.txt");
    private final String questionLongTemplate = resourceContent(getClass(), "question-long.txt");
    private final String questionGcpTemplate = resourceContent(getClass(), "question-gcp.txt");
    private final String definitionGrammarTemplate = resourceContent(getClass(), "definition-grammar.txt");
    private final String definitionShortTemplate = resourceContent(getClass(), "definition-short.txt");
    private final String definitionLongTemplate = resourceContent(getClass(), "definition-long.txt");
    private final String definitionGcpTemplate = resourceContent(getClass(), "definition-gcp.txt");
    private final String grammarTemplate = resourceContent(getClass(), "grammar.txt");
    private final String factGrammarTemplate = resourceContent(getClass(), "fact-grammar.txt");

    @Override
    public Optional<String> getPrompt(InteractionType interactionType, String theme, String question, AnswerType answerType) {
        return switch (interactionType) {
            case QUESTION -> switch (answerType) {
                case GRAMMAR -> Optional.of(format(grammarTemplate, question));
                case SHORT -> Optional.of(format(questionShortTemplate, theme, question));
                case LONG -> Optional.of(format(questionLongTemplate, theme, question));
                case GCP -> Optional.of(format(questionGcpTemplate, theme, question));
            };
            case DEFINITION -> switch (answerType) {
                case GRAMMAR -> Optional.of(format(definitionGrammarTemplate, theme, question));
                case SHORT -> Optional.of(format(definitionShortTemplate, question, theme));
                case LONG -> Optional.of(format(definitionLongTemplate, question, theme));
                case GCP -> Optional.of(format(definitionGcpTemplate, question, theme));
            };
            case GRAMMAR -> switch (answerType) {
                case GRAMMAR -> Optional.of(format(grammarTemplate, question));
                case SHORT, LONG, GCP -> Optional.empty();
            };
            case FACT -> switch (answerType) {
                case GRAMMAR -> Optional.of(format(factGrammarTemplate, theme, question));
                case SHORT, LONG, GCP -> Optional.empty();
            };
        };
    }
}


package gptui.model.question.prompt;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import gptui.model.storage.AnswerType;
import gptui.model.storage.InteractionType;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Optional;

@Singleton
@SuppressWarnings("TextBlockMigration")
class PromptFactoryImpl implements PromptFactory {
    private final Template questionShortTemplate;
    private final Template questionLongTemplate;
    private final Template questionGcpTemplate;
    private final Template definitionGrammarTemplate;
    private final Template definitionShortTemplate;
    private final Template definitionLongTemplate;
    private final Template definitionGcpTemplate;
    private final Template grammarTemplate;
    private final Template factGrammarTemplate;

    @Inject
    public PromptFactoryImpl(Configuration cfg) throws IOException {
        cfg.setClassForTemplateLoading(getClass(), "");
        questionShortTemplate = cfg.getTemplate("question-short.ftl");
        questionLongTemplate = cfg.getTemplate("question-long.ftl");
        questionGcpTemplate = cfg.getTemplate("question-gcp.ftl");
        definitionGrammarTemplate = cfg.getTemplate("definition-grammar.ftl");
        definitionShortTemplate = cfg.getTemplate("definition-short.ftl");
        definitionLongTemplate = cfg.getTemplate("definition-long.ftl");
        definitionGcpTemplate = cfg.getTemplate("definition-gcp.ftl");
        grammarTemplate = cfg.getTemplate("grammar.ftl");
        factGrammarTemplate = cfg.getTemplate("fact-grammar.ftl");
    }

    @Override
    public Optional<String> getPrompt(InteractionType interactionType, String theme, String question, AnswerType answerType) {
        var data = Map.of("theme", theme, "question", question);
        return switch (interactionType) {
            case QUESTION -> switch (answerType) {
                case GRAMMAR -> render(grammarTemplate, data);
                case SHORT -> render(questionShortTemplate, data);
                case LONG -> render(questionLongTemplate, data);
                case GCP -> render(questionGcpTemplate, data);
            };
            case DEFINITION -> switch (answerType) {
                case GRAMMAR -> render(definitionGrammarTemplate, data);
                case SHORT -> render(definitionShortTemplate, data);
                case LONG -> render(definitionLongTemplate, data);
                case GCP -> render(definitionGcpTemplate, data);
            };
            case GRAMMAR -> switch (answerType) {
                case GRAMMAR -> render(grammarTemplate, data);
                case SHORT, LONG, GCP -> Optional.empty();
            };
            case FACT -> switch (answerType) {
                case GRAMMAR -> render(factGrammarTemplate, data);
                case SHORT, LONG, GCP -> Optional.empty();
            };
        };
    }

    private static Optional<String> render(Template template, Map<String, String> data) {
        try {
            var out = new StringWriter();
            template.process(data, out);
            return Optional.of(out.toString());
        } catch (TemplateException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}


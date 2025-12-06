package gptui.model.question.prompt;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import gptui.model.config.ConfigModel;
import gptui.model.storage.AnswerType;
import gptui.model.storage.InteractionType;
import gptui.util.ResourceUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static freemarker.template.TemplateExceptionHandler.RETHROW_HANDLER;
import static java.nio.charset.StandardCharsets.UTF_8;

@Singleton
class PromptFactoryImpl implements PromptFactory {
    private static final Logger log = LoggerFactory.getLogger(PromptFactoryImpl.class);

    private static final String QUESTION_SHORT_TEMPLATE = "question-short.ftl";
    private static final String QUESTION_LONG_TEMPLATE = "question-long.ftl";
    private static final String QUESTION_GCP_TEMPLATE = "question-gcp.ftl";
    private static final String DEFINITION_GRAMMAR_TEMPLATE = "definition-grammar.ftl";
    private static final String DEFINITION_SHORT_TEMPLATE = "definition-short.ftl";
    private static final String DEFINITION_LONG_TEMPLATE = "definition-long.ftl";
    private static final String DEFINITION_GCP_TEMPLATE = "definition-gcp.ftl";
    private static final String GRAMMAR_TEMPLATE = "grammar.ftl";
    private static final String FACT_GRAMMAR_FTL = "fact-grammar.ftl";

    private static final StringTemplateLoader STRING_TEMPLATE_LOADER = new StringTemplateLoader();

    private final Path templatesDir;
    private final Configuration cfg;

    @Inject
    public PromptFactoryImpl(ConfigModel config) throws IOException {
        templatesDir = config.getAppDataPath().resolve("templates");
        Files.createDirectories(templatesDir);
        cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setDefaultEncoding(UTF_8.name());
        cfg.setTemplateExceptionHandler(RETHROW_HANDLER);
        cfg.setTemplateLoader(STRING_TEMPLATE_LOADER);
    }

    @Override
    public Optional<String> getPrompt(InteractionType interactionType, String theme, String question, AnswerType answerType) {
        var data = Map.of("theme", theme, "question", question);
        return switch (interactionType) {
            case QUESTION -> switch (answerType) {
                case GRAMMAR -> render(GRAMMAR_TEMPLATE, data);
                case SHORT -> render(QUESTION_SHORT_TEMPLATE, data);
                case LONG -> render(QUESTION_LONG_TEMPLATE, data);
                case GCP -> render(QUESTION_GCP_TEMPLATE, data);
            };
            case DEFINITION -> switch (answerType) {
                case GRAMMAR -> render(DEFINITION_GRAMMAR_TEMPLATE, data);
                case SHORT -> render(DEFINITION_SHORT_TEMPLATE, data);
                case LONG -> render(DEFINITION_LONG_TEMPLATE, data);
                case GCP -> render(DEFINITION_GCP_TEMPLATE, data);
            };
            case GRAMMAR -> switch (answerType) {
                case GRAMMAR -> render(GRAMMAR_TEMPLATE, data);
                case SHORT, LONG, GCP -> Optional.empty();
            };
            case FACT -> switch (answerType) {
                case GRAMMAR, SHORT -> Optional.empty();
                case LONG, GCP -> render(FACT_GRAMMAR_FTL, data);
            };
        };
    }

    private Optional<String> render(String templateName, Map<String, String> data) {
        try {
            var templatePath = templatesDir.resolve(templateName);
            if (Files.notExists(templatePath)) {
                try (var is = ResourceUtils.resourceIS(getClass(), templateName);
                     var out = Files.newOutputStream(templatePath)) {
                    is.transferTo(out);
                }
                log.info("Copied template '{}' to '{}'", templateName, templatePath);
            }
            var templateContent = Files.readString(templatePath);
            STRING_TEMPLATE_LOADER.putTemplate(templateName, templateContent);
            cfg.clearTemplateCache();
            var template = cfg.getTemplate(templateName);
            var out = new StringWriter();
            template.process(data, out);
            return Optional.of(out.toString());
        } catch (TemplateException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}


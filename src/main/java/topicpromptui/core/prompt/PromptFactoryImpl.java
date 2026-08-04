package topicpromptui.core.prompt;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import topicpromptui.core.config.ConfigModel;
import topicpromptui.core.domain.AnswerType;
import topicpromptui.core.domain.InteractionType;
import topicpromptui.core.util.ResourceUtils;
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

    // User-message templates are provider-independent (the system-prompt split moved every
    // provider/type difference into the system templates), so one per interaction type suffices.
    private static final String QUESTION_TEMPLATE = "question.ftl";
    private static final String DEFINITION_TEMPLATE = "definition.ftl";
    private static final String FACT_TEMPLATE = "fact.ftl";
    private static final String GRAMMAR_TEMPLATE = "grammar.ftl";

    // System prompts carry the stable behavioral instructions (shared across providers within a type)
    // that used to be baked inline into the user-message templates above.
    private static final String QUESTION_SYSTEM_TEMPLATE = "question-system.ftl";
    private static final String DEFINITION_SYSTEM_TEMPLATE = "definition-system.ftl";
    private static final String GRAMMAR_SYSTEM_TEMPLATE = "grammar-system.ftl";
    private static final String FACT_SYSTEM_TEMPLATE = "fact-system.ftl";

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
    public Optional<String> getPrompt(InteractionType interactionType, String question, AnswerType answerType) {
        var data = Map.of("question", question);
        return switch (interactionType) {
            case QUESTION -> switch (answerType) {
                case GRAMMAR -> render(GRAMMAR_TEMPLATE, data);
                case OPEN_AI, CLAUDE, GCP -> render(QUESTION_TEMPLATE, data);
            };
            case DEFINITION -> switch (answerType) {
                case GRAMMAR -> render(GRAMMAR_TEMPLATE, data);
                case OPEN_AI, CLAUDE, GCP -> render(DEFINITION_TEMPLATE, data);
            };
            case GRAMMAR -> switch (answerType) {
                case GRAMMAR -> render(GRAMMAR_TEMPLATE, data);
                case OPEN_AI, CLAUDE, GCP -> Optional.empty();
            };
            case FACT -> switch (answerType) {
                case GRAMMAR -> render(GRAMMAR_TEMPLATE, data);
                case OPEN_AI, CLAUDE, GCP -> render(FACT_TEMPLATE, data);
            };
        };
    }

    @Override
    public Optional<String> getSystemPrompt(InteractionType interactionType, String topic, AnswerType answerType) {
        var data = Map.of("topic", topic);
        return switch (interactionType) {
            case QUESTION -> switch (answerType) {
                case GRAMMAR -> render(GRAMMAR_SYSTEM_TEMPLATE, data);
                case OPEN_AI, CLAUDE, GCP -> render(QUESTION_SYSTEM_TEMPLATE, data);
            };
            case DEFINITION -> switch (answerType) {
                case GRAMMAR -> render(GRAMMAR_SYSTEM_TEMPLATE, data);
                case OPEN_AI, CLAUDE, GCP -> render(DEFINITION_SYSTEM_TEMPLATE, data);
            };
            case GRAMMAR -> switch (answerType) {
                case GRAMMAR -> render(GRAMMAR_SYSTEM_TEMPLATE, data);
                case OPEN_AI, CLAUDE, GCP -> Optional.empty();
            };
            case FACT -> switch (answerType) {
                case GRAMMAR -> render(GRAMMAR_SYSTEM_TEMPLATE, data);
                case OPEN_AI, CLAUDE, GCP -> render(FACT_SYSTEM_TEMPLATE, data);
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


package gptui.model.question.prompt;

import com.google.inject.AbstractModule;
import freemarker.template.Configuration;

import static freemarker.template.TemplateExceptionHandler.RETHROW_HANDLER;
import static java.nio.charset.StandardCharsets.UTF_8;

public class PromptModule extends AbstractModule {
    @Override
    protected void configure() {
        var cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setDefaultEncoding(UTF_8.name());
        cfg.setTemplateExceptionHandler(RETHROW_HANDLER);
        bind(Configuration.class).toInstance(cfg);
        bind(PromptFactory.class).to(PromptFactoryImpl.class);
    }
}

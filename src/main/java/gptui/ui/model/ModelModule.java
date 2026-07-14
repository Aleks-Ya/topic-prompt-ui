package gptui.ui.model;

import com.google.inject.AbstractModule;
import gptui.ui.model.clipboard.ClipboardModelModule;
import gptui.core.config.ConfigurationModule;
import gptui.ui.model.file.FileModelModule;
import gptui.core.ai.claude.ClaudeModule;
import gptui.core.ai.gcp.GcpModule;
import gptui.core.ai.openai.OpenAiModule;
import gptui.ui.model.question.prompt.PromptModule;
import gptui.ui.model.question.question.QuestionModule;
import gptui.ui.model.search.SearchModule;
import gptui.ui.model.state.StateModelModule;
import gptui.ui.model.storage.StorageModule;

public class ModelModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new FileModelModule());
        install(new ConfigurationModule());
        install(new OpenAiModule());
        install(new GcpModule());
        install(new ClaudeModule());
        install(new QuestionModule());
        install(new StorageModule());
        install(new StateModelModule());
        install(new ClipboardModelModule());
        install(new SearchModule());
        install(new PromptModule());
    }
}

package topicpromptui.ui.model;

import com.google.inject.AbstractModule;
import topicpromptui.ui.model.clipboard.ClipboardModelModule;
import topicpromptui.core.config.ConfigurationModule;
import topicpromptui.ui.model.file.FileModelModule;
import topicpromptui.core.ai.claude.ClaudeModule;
import topicpromptui.core.ai.gcp.GcpModule;
import topicpromptui.core.ai.openai.OpenAiModule;
import topicpromptui.ui.model.question.prompt.PromptModule;
import topicpromptui.ui.model.question.question.QuestionModule;
import topicpromptui.ui.model.search.SearchModule;
import topicpromptui.ui.model.state.StateModelModule;
import topicpromptui.ui.model.storage.StorageModule;

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

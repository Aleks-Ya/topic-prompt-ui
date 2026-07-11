package gptui.ui.viewmodel.answer;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import gptui.core.storagefilesystem.AnswerType;

public class AnswerVmModule extends AbstractModule {
    public static final String GRAMMAR = "GrammarAnswerVM";
    public static final String OPEN_AI = "OpenAiAnswerVM";
    public static final String CLAUDE = "ClaudeAnswerVM";
    public static final String GCP = "GcpAnswerVM";

    @Override
    protected void configure() {
        var grammarAnswer = new AnswerVmImpl(AnswerType.GRAMMAR);
        var openAiAnswer = new AnswerVmImpl(AnswerType.OPEN_AI);
        var claudeAnswer = new AnswerVmImpl(AnswerType.CLAUDE);
        var gcpAnswer = new AnswerVmImpl(AnswerType.GCP);

        bind(AnswerVmController.class).annotatedWith(Names.named(GRAMMAR)).toInstance(grammarAnswer);
        bind(AnswerVmController.class).annotatedWith(Names.named(OPEN_AI)).toInstance(openAiAnswer);
        bind(AnswerVmController.class).annotatedWith(Names.named(CLAUDE)).toInstance(claudeAnswer);
        bind(AnswerVmController.class).annotatedWith(Names.named(GCP)).toInstance(gcpAnswer);

        bind(AnswerVmMediator.class).annotatedWith(Names.named(GRAMMAR)).toInstance(grammarAnswer);
        bind(AnswerVmMediator.class).annotatedWith(Names.named(OPEN_AI)).toInstance(openAiAnswer);
        bind(AnswerVmMediator.class).annotatedWith(Names.named(CLAUDE)).toInstance(claudeAnswer);
        bind(AnswerVmMediator.class).annotatedWith(Names.named(GCP)).toInstance(gcpAnswer);
    }
}

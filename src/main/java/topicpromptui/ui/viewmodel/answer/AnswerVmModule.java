package topicpromptui.ui.viewmodel.answer;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import topicpromptui.core.domain.AnswerType;

public class AnswerVmModule extends AbstractModule {
    public static final String GRAMMAR = "GrammarAnswerVM";
    public static final String AI_1 = "Ai1AnswerVM";
    public static final String AI_2 = "Ai2AnswerVM";
    public static final String AI_3 = "Ai3AnswerVM";

    @Override
    protected void configure() {
        var grammarAnswer = new AnswerVmImpl(AnswerType.GRAMMAR);
        var ai1Answer = new AnswerVmImpl(AnswerType.AI_1);
        var ai2Answer = new AnswerVmImpl(AnswerType.AI_2);
        var ai3Answer = new AnswerVmImpl(AnswerType.AI_3);

        bind(AnswerVmController.class).annotatedWith(Names.named(GRAMMAR)).toInstance(grammarAnswer);
        bind(AnswerVmController.class).annotatedWith(Names.named(AI_1)).toInstance(ai1Answer);
        bind(AnswerVmController.class).annotatedWith(Names.named(AI_2)).toInstance(ai2Answer);
        bind(AnswerVmController.class).annotatedWith(Names.named(AI_3)).toInstance(ai3Answer);

        bind(AnswerVmMediator.class).annotatedWith(Names.named(GRAMMAR)).toInstance(grammarAnswer);
        bind(AnswerVmMediator.class).annotatedWith(Names.named(AI_1)).toInstance(ai1Answer);
        bind(AnswerVmMediator.class).annotatedWith(Names.named(AI_2)).toInstance(ai2Answer);
        bind(AnswerVmMediator.class).annotatedWith(Names.named(AI_3)).toInstance(ai3Answer);
    }
}

package topicpromptui.ui.viewmodel.answer;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
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

        // The view still resolves its pane view models per fx:id, so these keep their @Named qualifiers.
        bind(AnswerVmController.class).annotatedWith(Names.named(GRAMMAR)).toInstance(grammarAnswer);
        bind(AnswerVmController.class).annotatedWith(Names.named(AI_1)).toInstance(ai1Answer);
        bind(AnswerVmController.class).annotatedWith(Names.named(AI_2)).toInstance(ai2Answer);
        bind(AnswerVmController.class).annotatedWith(Names.named(AI_3)).toInstance(ai3Answer);

        // Unlike AiProviderModule, whose map entries link to existing @Named bindings, these bind the
        // instances directly: the mediator addresses panes only by AnswerType, so no @Named
        // AnswerVmMediator binding is left to link to.
        var mediators = MapBinder.newMapBinder(binder(), AnswerType.class, AnswerVmMediator.class);
        mediators.addBinding(AnswerType.GRAMMAR).toInstance(grammarAnswer);
        mediators.addBinding(AnswerType.AI_1).toInstance(ai1Answer);
        mediators.addBinding(AnswerType.AI_2).toInstance(ai2Answer);
        mediators.addBinding(AnswerType.AI_3).toInstance(ai3Answer);
    }
}

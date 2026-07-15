package topicpromptui.ui.model.state;

import com.google.inject.AbstractModule;

public class StateModelModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(StateModel.class).to(StateModelImpl.class);
    }
}

package topicpromptui;

import com.google.inject.AbstractModule;
import topicpromptui.ui.model.ModelModule;
import topicpromptui.ui.view.ViewModule;
import topicpromptui.ui.viewmodel.ViewModelModule;

public class RootModule extends AbstractModule {
    @Override
    protected void configure() {
        binder().requireExplicitBindings();
        install(new ModelModule());
        install(new ViewModelModule());
        install(new ViewModule());
    }
}

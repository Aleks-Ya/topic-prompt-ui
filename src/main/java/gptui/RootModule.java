package gptui;

import com.google.inject.AbstractModule;
import gptui.ui.model.ModelModule;
import gptui.ui.view.ViewModule;
import gptui.ui.viewmodel.ViewModelModule;

public class RootModule extends AbstractModule {
    @Override
    protected void configure() {
        binder().requireExplicitBindings();
        install(new ModelModule());
        install(new ViewModelModule());
        install(new ViewModule());
    }
}

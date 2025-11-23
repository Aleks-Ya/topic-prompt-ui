package gptui;

import com.google.inject.Guice;
import com.google.inject.Injector;

import static com.google.inject.util.Modules.override;

public abstract class BaseTest {
    protected final Injector injector = Guice.createInjector(override(new RootModule()).with(new TestRootModule()));
}
package gptui;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

public abstract class BaseTest {
    protected Injector injector = Guice.createInjector(Modules.override(new RootModule()).with(new TestRootModule()));
}
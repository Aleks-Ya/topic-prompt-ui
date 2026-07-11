package gptui.core.ai.gcp;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import gptui.core.ai.AiApi;

import static gptui.core.ai.AiModule.GCP_AI;

public class GcpModule extends AbstractModule {
    private static final String MODEL = "gemini-3.1-pro-preview";

    @Override
    protected void configure() {
        bind(AiApi.class).annotatedWith(Names.named(GCP_AI)).toInstance(new GcpApiImpl(MODEL));
    }
}

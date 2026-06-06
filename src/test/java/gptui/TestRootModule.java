package gptui;

import com.google.common.jimfs.Jimfs;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import gptui.core.ai.AiApi;
import gptui.core.ai.gcp.MockGcpApi;
import gptui.core.ai.openai.MockOpenAiApi;
import gptui.model.question.sound.SoundService;
import gptui.model.question.sound.SoundServiceMock;

import java.nio.file.FileSystem;

import static com.google.common.jimfs.Configuration.unix;
import static gptui.core.ai.AiModule.GCP_AI;
import static gptui.core.ai.AiModule.OPEN_AI;

public class TestRootModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(MockOpenAiApi.class);
        bind(MockGcpApi.class);
        bind(AiApi.class).annotatedWith(Names.named(OPEN_AI)).to(MockOpenAiApi.class);
        bind(AiApi.class).annotatedWith(Names.named(GCP_AI)).to(MockGcpApi.class);
        bind(SoundService.class).to(SoundServiceMock.class);
        bind(FileSystem.class).toInstance(Jimfs.newFileSystem(unix()));
    }
}

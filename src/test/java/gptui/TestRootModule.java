package gptui;

import com.google.common.jimfs.Jimfs;
import com.google.inject.AbstractModule;
import gptui.model.question.gcp.GcpApi;
import gptui.model.question.gcp.MockGcpApi;
import gptui.model.question.openai.MockOpenAiApi;
import gptui.model.question.openai.OpenAiApi;
import gptui.model.question.sound.SoundService;
import gptui.model.question.sound.SoundServiceMock;

import java.nio.file.FileSystem;

import static com.google.common.jimfs.Configuration.unix;

public class TestRootModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(MockOpenAiApi.class);
        bind(MockGcpApi.class);
        bind(OpenAiApi.class).to(MockOpenAiApi.class);
        bind(GcpApi.class).to(MockGcpApi.class);
        bind(SoundService.class).to(SoundServiceMock.class);
        bind(FileSystem.class).toInstance(Jimfs.newFileSystem(unix()));
    }
}

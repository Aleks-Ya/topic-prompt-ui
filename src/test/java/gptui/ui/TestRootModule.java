package gptui.ui;

import com.google.common.jimfs.Jimfs;
import com.google.inject.AbstractModule;
import gptui.model.question.gcp.GcpApi;
import gptui.model.question.openai.OpenAiApi;
import gptui.model.question.openai.MockOpenAiApi;

import java.nio.file.FileSystem;

import static com.google.common.jimfs.Configuration.unix;

class TestRootModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(MockOpenAiApi.class);
        bind(OpenAiApi.class).to(MockOpenAiApi.class);
        bind(GcpApi.class).to(MockOpenAiApi.class);
        bind(FileSystem.class).toInstance(Jimfs.newFileSystem(unix()));
    }
}

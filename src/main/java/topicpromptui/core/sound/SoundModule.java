package topicpromptui.core.sound;

import com.google.inject.AbstractModule;

public class SoundModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(SoundService.class).to(SoundServiceImpl.class);
    }
}

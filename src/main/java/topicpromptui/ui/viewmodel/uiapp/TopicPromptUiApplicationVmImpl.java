package topicpromptui.ui.viewmodel.uiapp;

import topicpromptui.ui.viewmodel.mediator.TopicPromptUiApplicationMediator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.collections.ObservableMap;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

@Singleton
class TopicPromptUiApplicationVmImpl implements TopicPromptUiApplicationVmController {
    private static final Logger log = LoggerFactory.getLogger(TopicPromptUiApplicationVmImpl.class);
    private final TopicPromptUiApplicationMediator mediator;

    @Inject
    TopicPromptUiApplicationVmImpl(TopicPromptUiApplicationMediator mediator) {
        this.mediator = mediator;
    }

    public Image getApplicationIcon() {
        log.info("Loading application icon...");
        var applicationIcon = new Image(mediator.getAppIcon());
        log.info("Application icon: {}", applicationIcon);
        return applicationIcon;
    }

    public String getAppVersion() {
        return mediator.getAppVersion();
    }

    public URL getFxmlLocation() {
        return mediator.getFxmlLocation();
    }

    public void stageShowed(ObservableMap<KeyCombination, Runnable> accelerators) {
        mediator.addShortcuts(accelerators);
        mediator.stageShowed();
    }
}


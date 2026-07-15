package topicpromptui.ui.view;

import com.gluonhq.ignite.guice.GuiceContext;
import com.google.inject.Module;
import topicpromptui.RootModule;
import topicpromptui.ui.viewmodel.uiapp.TopicPromptUiApplicationVmController;
import jakarta.inject.Inject;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

// JavaFX's Application lifecycle requires a public no-arg constructor and instantiates this class
// itself (not Guice), so dependencies can only arrive via GuiceContext.init() member injection,
// not constructor injection.
@SuppressWarnings("java:S6813")
public class TopicPromptUiApplication extends Application {
    private static final Logger log = LoggerFactory.getLogger(TopicPromptUiApplication.class);
    private final GuiceContext context;
    @Inject
    private FXMLLoader fxmlLoader;
    @Inject
    private TopicPromptUiApplicationVmController vm;

    public TopicPromptUiApplication() {
        this(new RootModule());
    }

    public TopicPromptUiApplication(Module module) {
        context = new GuiceContext(this, () -> List.of(module));
        context.init();
    }

    @Override
    public void start(Stage stage) throws Exception {
        try {
            var scene = createScene();
            var version = vm.getAppVersion();
            createStage(stage, version, scene);
            vm.stageShowed(scene.getAccelerators());
        } catch (Exception e) {
            log.error("Starting application error", e);
            throw new IllegalStateException("Failed to start application", e);
        }
    }

    private Scene createScene() throws IOException {
        fxmlLoader.setLocation(vm.getFxmlLocation());
        Parent root = fxmlLoader.load();
        return new Scene(root, Color.LIGHTYELLOW);
    }

    private void createStage(Stage stage, String version, Scene scene) {
        stage.setTitle("GPT-4 Question Client v" + version);
        stage.setScene(scene);
        stage.setMaximized(true);
        var applicationIcon = vm.getApplicationIcon();
        stage.getIcons().add(applicationIcon);
        stage.show();
    }

    public GuiceContext getGuiceContext() {
        return context;
    }
}

package topicpromptui.ui.view;

import topicpromptui.core.storagefilesystem.AnswerType;
import topicpromptui.ui.viewmodel.answer.AnswerVmController;
import topicpromptui.ui.viewmodel.answer.AnswerVmModule;
import topicpromptui.ui.viewmodel.ui.TopicPromptUiVmController;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import javafx.fxml.FXML;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FXMLLoader instantiates this controller itself (via its no-arg constructor) and Gluon Ignite's
// GuiceContext only performs member injection on it afterward, so constructor injection isn't an
// option here — same reasoning applies to every *Controller class in this package.
@SuppressWarnings("java:S6813")
public class TopicPromptUiController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(TopicPromptUiController.class);
    @Inject
    private TopicPromptUiVmController vm;
    @FXML
    private VBox rootVBox;
    @FXML
    private HBox grammarAnswer;
    @FXML
    private HBox openAiAnswer;
    @FXML
    private HBox claudeAnswer;
    @FXML
    private HBox gcpAnswer;
    @FXML
    @SuppressWarnings("unused")
    private AnswerController grammarAnswerController;
    @FXML
    @SuppressWarnings("unused")
    private AnswerController claudeAnswerController;
    @FXML
    @SuppressWarnings("unused")
    private AnswerController openAiAnswerController;
    @FXML
    @SuppressWarnings("unused")
    private AnswerController gcpAnswerController;
    @Inject
    @Named(AnswerVmModule.GRAMMAR)
    private AnswerVmController grammarAnswerVM;
    @Inject
    @Named(AnswerVmModule.OPEN_AI)
    private AnswerVmController openAiAnswerVM;
    @Inject
    @Named(AnswerVmModule.CLAUDE)
    private AnswerVmController claudeAnswerVM;
    @Inject
    @Named(AnswerVmModule.GCP)
    private AnswerVmController gcpAnswerVM;
    private HBox expandedPane;
    private double savedMaxHeight;
    private Priority savedVgrow;

    @Override
    protected void initialize() {
        log.trace("initialize");
        grammarAnswerController.initializeController(grammarAnswerVM);
        openAiAnswerController.initializeController(openAiAnswerVM);
        claudeAnswerController.initializeController(claudeAnswerVM);
        gcpAnswerController.initializeController(gcpAnswerVM);
        vm.properties().expandedAnswerType.addListener((_, _, newType) -> onExpandedAnswerChanged(newType));
        vm.initialize();
    }

    private void onExpandedAnswerChanged(AnswerType newType) {
        log.trace("onExpandedAnswerChanged: {}", newType);
        if (expandedPane != null) {
            expandedPane.setMaxHeight(savedMaxHeight);
            VBox.setVgrow(expandedPane, savedVgrow);
            expandedPane = null;
        }
        if (newType != null) {
            expandedPane = paneFor(newType);
            savedMaxHeight = expandedPane.getMaxHeight();
            savedVgrow = VBox.getVgrow(expandedPane);
            expandedPane.setMaxHeight(Double.MAX_VALUE);
            VBox.setVgrow(expandedPane, Priority.ALWAYS);
        }
        for (var child : rootVBox.getChildren()) {
            var show = expandedPane == null || child == expandedPane;
            child.setManaged(show);
            // The Separators are invisible spacers (visible="false" in the FXML) and must stay invisible
            // when the normal layout is restored; unmanaging them is enough to reclaim their space.
            if (!(child instanceof Separator)) {
                child.setVisible(show);
            }
        }
    }

    private HBox paneFor(AnswerType answerType) {
        return switch (answerType) {
            case GRAMMAR -> grammarAnswer;
            case OPEN_AI -> openAiAnswer;
            case CLAUDE -> claudeAnswer;
            case GCP -> gcpAnswer;
        };
    }
}
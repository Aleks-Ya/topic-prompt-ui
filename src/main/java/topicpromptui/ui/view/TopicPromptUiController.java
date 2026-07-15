package topicpromptui.ui.view;

import topicpromptui.ui.viewmodel.answer.AnswerVmController;
import topicpromptui.ui.viewmodel.answer.AnswerVmModule;
import topicpromptui.ui.viewmodel.ui.TopicPromptUiVmController;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import javafx.fxml.FXML;
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

    @Override
    protected void initialize() {
        log.trace("initialize");
        grammarAnswerController.initializeController(grammarAnswerVM);
        openAiAnswerController.initializeController(openAiAnswerVM);
        claudeAnswerController.initializeController(claudeAnswerVM);
        gcpAnswerController.initializeController(gcpAnswerVM);
        vm.initialize();
    }
}
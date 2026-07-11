package gptui.ui.view;

import gptui.ui.viewmodel.answer.AnswerVmController;
import gptui.ui.viewmodel.answer.AnswerVmModule;
import gptui.ui.viewmodel.ui.GptUiVmController;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import javafx.fxml.FXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GptUiController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(GptUiController.class);
    @Inject
    private GptUiVmController vm;
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
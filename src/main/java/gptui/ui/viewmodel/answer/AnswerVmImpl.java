package gptui.ui.viewmodel.answer;

import gptui.core.util.Mdc;
import gptui.core.storagefilesystem.Answer;
import gptui.core.storagefilesystem.AnswerState;
import gptui.core.storagefilesystem.AnswerType;
import gptui.ui.viewmodel.mediator.AnswerMediator;
import jakarta.inject.Inject;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static gptui.core.storagefilesystem.AnswerState.NEW;
import static gptui.core.storagefilesystem.AnswerType.CLAUDE;
import static gptui.core.storagefilesystem.AnswerType.GCP;
import static gptui.core.storagefilesystem.AnswerType.GRAMMAR;
import static gptui.core.storagefilesystem.AnswerType.OPEN_AI;
import static javafx.scene.paint.Color.BLUE;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.RED;
import static javafx.scene.paint.Color.WHITE;

class AnswerVmImpl implements AnswerVmController, AnswerVmMediator {
    public AnswerVmImpl(AnswerType answerType) {
        this.answerType = answerType;
    }

    private static final Logger log = LoggerFactory.getLogger(AnswerVmImpl.class);
    public final AnswerVmProperties vmProperties = new AnswerVmProperties();
    @Inject
    private AnswerMediator mediator;
    private String currentWebViewContent = "";
    private final AnswerType answerType;
    private static final Map<AnswerType, Integer> hotkeyDigitMap = Map.of(GRAMMAR, 1, OPEN_AI, 2, CLAUDE, 3, GCP, 4);
    private static final Map<AnswerType, String> buttonTextMap = Map.of(GRAMMAR, "Grammar\nanswer:", OPEN_AI, "OpenAI\nanswer:", CLAUDE, "Claude\nanswer:", GCP, "GCP\nanswer:");

    @Override
    public void onCopyButtonClick() {
        Mdc.run(answerType, () -> {
            log.trace("onCopyButtonClick");
            var content = vmProperties.webViewContent.get();
            mediator.putHtmlToClipboard(content);
        });
    }

    @Override
    public void onRegenerateButtonClick() {
        log.trace("onRegenerateButtonClick");
        mediator.requestAnswer(mediator.getCurrentInteractionId(), answerType);
    }

    @Override
    public AnswerVmProperties properties() {
        return vmProperties;
    }

    @Override
    public AnswerDetails getAnswerDetails() {
        return mediator.getCurrentInteractionOpt()
                .flatMap(interaction -> interaction.getAnswer(answerType))
                .map(a -> new AnswerDetails(a.answerType(), a.modelId(), a.effortLevel(), a.finishReason(),
                        a.inputTokens(), a.outputTokens(), a.totalTokens(), a.prompt()))
                .orElse(new AnswerDetails(answerType, null, null, null, null, null, null, null));
    }

    @Override
    public void displayCurrentAnswer() {
        Mdc.run(answerType, () -> {
            log.trace("displayCurrentAnswer");
            mediator.getCurrentInteractionOpt().map(interaction -> interaction.getAnswer(answerType)).ifPresentOrElse(answerOpt -> {
                log.trace("Display answer: {}", answerOpt.map(Answer::toShortString));
                var html = answerOpt.isPresent() ? answerOpt.get().answerHtml() : "";
                var state = answerOpt.isPresent() ? answerOpt.get().answerState() : NEW;
                if (!currentWebViewContent.equals(html)) {
                    vmProperties.webViewContent.set(html);
                    currentWebViewContent = html;
                }
                vmProperties.statusCircleFill.setValue(answerStateToColor(state));
            }, () -> {
                log.trace("Display empty answer");
                currentWebViewContent = "";
                vmProperties.webViewContent.set("");
                vmProperties.statusCircleFill.setValue(WHITE);
            });
        });
    }

    @Override
    public void initialize() {
        Mdc.run(answerType, () -> {
            log.trace("displayInitialState");
            vmProperties.answerButtonText.setValue(buttonTextMap.get(answerType));
            vmProperties.copyButtonText.setValue(vmProperties.copyButtonText.getValue() + " _" + hotkeyDigitMap.get(answerType));
        });
    }

    @Override
    public void ctrlAltUpHotkeyPressed() {
        mediator.selectNextHistoryItem();
    }

    @Override
    public void ctrlAltDownHotkeyPressed() {
        mediator.selectNextHistoryItem();
    }

    private Color answerStateToColor(AnswerState answerState) {
        return switch (answerState) {
            case NEW -> WHITE;
            case SENT -> BLUE;
            case SUCCESS -> GREEN;
            case FAIL -> RED;
        };
    }

}


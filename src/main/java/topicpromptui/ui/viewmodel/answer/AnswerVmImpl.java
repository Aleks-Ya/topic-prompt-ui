package topicpromptui.ui.viewmodel.answer;

import topicpromptui.core.util.Mdc;
import topicpromptui.core.storagefilesystem.Answer;
import topicpromptui.core.storagefilesystem.AnswerState;
import topicpromptui.core.storagefilesystem.AnswerType;
import topicpromptui.ui.viewmodel.mediator.AnswerMediator;
import jakarta.inject.Inject;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static topicpromptui.core.storagefilesystem.AnswerState.NEW;
import static topicpromptui.core.storagefilesystem.AnswerType.CLAUDE;
import static topicpromptui.core.storagefilesystem.AnswerType.GCP;
import static topicpromptui.core.storagefilesystem.AnswerType.GRAMMAR;
import static topicpromptui.core.storagefilesystem.AnswerType.OPEN_AI;
import static javafx.scene.paint.Color.BLUE;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.RED;
import static javafx.scene.paint.Color.WHITE;

// AnswerVmModule builds the 4 parallel instances manually (new AnswerVmImpl(answerType)) and binds
// them via toInstance(...) so each is wired to its own @Named AnswerType; Guice therefore never
// calls this constructor and can only supply mediator via member injection.
@SuppressWarnings("java:S6813")
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
    private static final Map<AnswerType, String> buttonTextMap = Map.of(GRAMMAR, "Grammar:", OPEN_AI, "OpenAI:", CLAUDE, "Claude:", GCP, "Gemini:");

    @Override
    public void onCopyButtonClick() {
        Mdc.run(answerType.toString(), () -> {
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
    public void onExpandButtonClick() {
        Mdc.run(answerType.toString(), () -> {
            log.trace("onExpandButtonClick");
            mediator.toggleExpandedAnswer(answerType);
        });
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
        Mdc.run(answerType.toString(), () -> {
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
    public void displayPartialAnswer(String html) {
        Mdc.run(answerType.toString(), () -> {
            log.trace("displayPartialAnswer: {} chars", html.length());
            vmProperties.webViewContent.set(html);
            currentWebViewContent = html;
        });
    }

    @Override
    public void initialize() {
        Mdc.run(answerType.toString(), () -> {
            log.trace("displayInitialState");
            vmProperties.answerButtonText.setValue(buttonTextMap.get(answerType));
            vmProperties.copyButtonText.setValue(vmProperties.copyButtonText.getValue() + " _" + hotkeyDigitMap.get(answerType));
        });
    }

    @Override
    public void ctrlDigitHotkeyPressed(int digit) {
        Mdc.run(answerType.toString(), () -> {
            log.trace("ctrlDigitHotkeyPressed: {}", digit);
            // The digit, not the receiving VM instance, picks the target pane
            hotkeyDigitMap.entrySet().stream()
                    .filter(entry -> entry.getValue() == digit)
                    .findFirst()
                    .ifPresent(entry -> mediator.toggleExpandedAnswer(entry.getKey()));
        });
    }

    @Override
    public void ctrlAltUpHotkeyPressed() {
        mediator.selectPreviousHistoryItem();
    }

    @Override
    public void ctrlAltDownHotkeyPressed() {
        mediator.selectNextHistoryItem();
    }

    @Override
    public void ctrlFHotkeyPressed() {
        mediator.focusHistoryFilter();
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


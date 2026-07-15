package topicpromptui.ui.viewmodel.answer;

public interface AnswerVmController {
    void onCopyButtonClick();

    void onRegenerateButtonClick();

    AnswerVmProperties properties();

    AnswerDetails getAnswerDetails();

    void ctrlAltUpHotkeyPressed();

    void ctrlAltDownHotkeyPressed();
}

package topicpromptui.ui.viewmodel.answer;

public interface AnswerVmController {
    void onCopyButtonClick();

    void onRegenerateButtonClick();

    void onExpandButtonClick();

    void onOpenInteractionFileButtonClick();

    AnswerVmProperties properties();

    AnswerDetails getAnswerDetails();

    void ctrlAltUpHotkeyPressed();

    void ctrlAltDownHotkeyPressed();

    void ctrlDigitHotkeyPressed(int digit);

    void ctrlFHotkeyPressed();
}

package topicpromptui.ui.viewmodel.answer;

public interface AnswerVmController {
    void onCopyButtonClick();

    void onRegenerateButtonClick();

    void onExpandButtonClick();

    /** The pane's provider ComboBox fired: applies its value if it differs from the current selection. */
    void onProviderSelected();

    /** A link inside the answer was clicked; opens it outside the WebView. */
    void onSourceLinkClick(String url);

    void onOpenInteractionFileButtonClick();

    AnswerVmProperties properties();

    AnswerDetails getAnswerDetails();

    void ctrlAltUpHotkeyPressed();

    void ctrlAltDownHotkeyPressed();

    void ctrlDigitHotkeyPressed(int digit);

    void ctrlFHotkeyPressed();
}

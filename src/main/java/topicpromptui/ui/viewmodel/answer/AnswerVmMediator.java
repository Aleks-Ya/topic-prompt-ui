package topicpromptui.ui.viewmodel.answer;

public interface AnswerVmMediator {
    void displayCurrentAnswer();

    /**
     * Shows the stored final answer after its stream completed, updating the WebView in place so
     * the user's scroll position survives the partial→final swap. All other call sites (history
     * switching, topic changes, new interactions) must keep using {@link #displayCurrentAnswer()},
     * which loads fresh content with the scroll at the top.
     */
    void displayCompletedAnswer();

    /**
     * Shows a partial-answer HTML snapshot while the answer is still streaming.
     * Must be called on the JavaFX Application Thread. The status circle is left
     * untouched (stays SENT/blue) — the final state comes via {@link #displayCurrentAnswer()}.
     */
    void displayPartialAnswer(String html);

    void initialize();
}

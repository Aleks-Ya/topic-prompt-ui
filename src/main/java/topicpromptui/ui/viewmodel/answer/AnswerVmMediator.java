package topicpromptui.ui.viewmodel.answer;

public interface AnswerVmMediator {
    void displayCurrentAnswer();

    /**
     * Shows a partial-answer HTML snapshot while the answer is still streaming.
     * Must be called on the JavaFX Application Thread. The status circle is left
     * untouched (stays SENT/blue) — the final state comes via {@link #displayCurrentAnswer()}.
     */
    void displayPartialAnswer(String html);

    void initialize();
}

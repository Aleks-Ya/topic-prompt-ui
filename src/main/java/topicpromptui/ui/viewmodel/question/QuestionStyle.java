package topicpromptui.ui.viewmodel.question;

/**
 * Style classes for the question TextArea, defined in {@code topicpromptui/ui/view/app.css}.
 * QUESTION_STYLE_EMPTY has no rule in the stylesheet — it exists so exactly one of the three is always
 * applied, which keeps swapping them a plain remove-then-add and keeps the assertions uniform.
 */
public final class QuestionStyle {
    public static final String QUESTION_STYLE_EMPTY = "question-empty";
    public static final String QUESTION_STYLE_EDITED = "question-edited";
    public static final String QUESTION_STYLE_FOLLOW_UP = "question-follow-up";

    private QuestionStyle() {
    }
}

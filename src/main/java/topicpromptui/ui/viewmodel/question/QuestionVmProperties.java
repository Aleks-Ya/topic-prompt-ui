package topicpromptui.ui.viewmodel.question;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class QuestionVmProperties {
    public final StringProperty questionTaText = new SimpleStringProperty();
    // Seeded so exactly one question-* style class is applied from the start, not only once the view
    // model first writes one.
    public final StringProperty questionTaStyle = new SimpleStringProperty(QuestionStyle.QUESTION_STYLE_EMPTY);
    public final BooleanProperty questionTaFocused = new SimpleBooleanProperty();
    public final BooleanProperty questionTaSelectAll = new SimpleBooleanProperty();
    public final BooleanProperty questionTaPositionCaretToEnd = new SimpleBooleanProperty();
    public final BooleanProperty followUpCheckBoxSelected = new SimpleBooleanProperty();
}

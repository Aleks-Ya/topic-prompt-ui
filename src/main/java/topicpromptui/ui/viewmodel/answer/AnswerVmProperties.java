package topicpromptui.ui.viewmodel.answer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Paint;

public class AnswerVmProperties {
    public final StringProperty webViewContent = new SimpleStringProperty();
    /**
     * Modifier for the next {@link #webViewContent} write: when true, the view must update the
     * WebView's body in place (preserving the scroll position) instead of reloading the document.
     * Deliberately not a JavaFX property — it is set and cleared synchronously around the write on
     * the FX thread by AnswerVmImpl and never observed on its own.
     */
    public boolean preserveScrollOnNextUpdate = false;
    public final StringProperty answerButtonText = new SimpleStringProperty();
    public final StringProperty copyButtonText = new SimpleStringProperty();
    public final ObjectProperty<Paint> statusCircleFill = new SimpleObjectProperty<>();
}

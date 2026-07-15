package topicpromptui.ui.viewmodel.answer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Paint;

public class AnswerVmProperties {
    public final StringProperty webViewContent = new SimpleStringProperty();
    public final StringProperty answerButtonText = new SimpleStringProperty();
    public final StringProperty copyButtonText = new SimpleStringProperty();
    public final ObjectProperty<Paint> statusCircleFill = new SimpleObjectProperty<>();
}

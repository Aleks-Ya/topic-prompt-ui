package topicpromptui.ui.viewmodel.ui;

import topicpromptui.core.storagefilesystem.AnswerType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class TopicPromptUiVmProperties {
    // null means no answer pane is expanded (the normal layout with all panes visible)
    public final ObjectProperty<AnswerType> expandedAnswerType = new SimpleObjectProperty<>(null);
}

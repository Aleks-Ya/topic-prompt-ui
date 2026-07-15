package gptui.ui.viewmodel.topic;

import gptui.core.storagefilesystem.Topic;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class TopicVmProperties {
    public final ObjectProperty<Topic> topicCbValue = new SimpleObjectProperty<>();
    public final ListProperty<Topic> topicCbItems = new SimpleListProperty<>();
    public final StringProperty topicCbEditor = new SimpleStringProperty();
    public final ObjectProperty<Callback<ListView<Topic>, ListCell<Topic>>> topicCbCellFactory = new SimpleObjectProperty<>();
    public final BooleanProperty filterHistoryCheckBoxSelected = new SimpleBooleanProperty();
    public final StringProperty topicLabelText = new SimpleStringProperty();
    public final BooleanProperty renameButtonDisable = new SimpleBooleanProperty();
}

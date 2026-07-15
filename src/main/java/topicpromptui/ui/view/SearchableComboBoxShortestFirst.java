package topicpromptui.ui.view;

import javafx.collections.transformation.SortedList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.controlsfx.control.SearchableComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

/**
 * Orders the suggestions in a {@link SearchableComboBox} popup shortest-display-text-first while the
 * user is typing in its search field, so an exact match like "AWS" ranks above "AWS CloudFormation".
 * When the search text is empty the items' own order is left untouched.
 * <p>
 * ControlsFX's SearchableComboBoxSkin only filters (a fresh FilteredList per keystroke, source order
 * preserved) and its members are private in a non-exported package, so this hooks the skin's child
 * nodes by their stable ids: the internal ComboBox "#filtered" and the search TextField "#search".
 * Re-verify those ids on ControlsFX upgrades.
 */
final class SearchableComboBoxShortestFirst {
    private static final Logger log = LoggerFactory.getLogger(SearchableComboBoxShortestFirst.class);

    private SearchableComboBoxShortestFirst() {
    }

    static <T> void attach(SearchableComboBox<T> comboBox) {
        comboBox.skinProperty().addListener((_, _, newSkin) -> {
            if (newSkin != null) {
                hook(comboBox);
            }
        });
        if (comboBox.getSkin() != null) {
            hook(comboBox);
        }
    }

    private static <T> void hook(SearchableComboBox<T> comboBox) {
        var filteredNode = comboBox.lookup("#filtered");
        var searchNode = comboBox.lookup("#search");
        if (!(filteredNode instanceof ComboBox<?> filteredRaw) || !(searchNode instanceof TextField searchField)) {
            log.warn("SearchableComboBox skin internals '#filtered'/'#search' not found "
                    + "(ControlsFX skin structure changed?); shortest-first popup ordering disabled");
            return;
        }
        @SuppressWarnings("unchecked")
        var filtered = (ComboBox<T>) filteredRaw;
        log.debug("Hooked shortest-first ordering into SearchableComboBox {}", comboBox.getId());
        var byDisplayTextLength = Comparator.<T>comparingInt(item -> displayText(filtered, item).length());
        // Must be an InvalidationListener: the skin replaces one FilteredList with another whose content is
        // often equals() (AbstractList compares element-wise), and ChangeListeners are suppressed for equal values.
        filtered.itemsProperty().addListener(_ -> {
            var items = filtered.getItems();
            if (items == null || items instanceof SortedList<T>) {
                return; // SortedList means our own setItems below triggered this listener
            }
            if (!searchField.getText().trim().isEmpty()) {
                log.trace("Sorting {} suggestions shortest-first for search text '{}'", items.size(), searchField.getText());
                // stable sort: equal lengths keep the source (most-recently-used) order
                filtered.setItems(new SortedList<>(items, byDisplayTextLength));
            }
        });
    }

    private static <T> String displayText(ComboBox<T> comboBox, T item) {
        if (item == null) {
            return "";
        }
        var converter = comboBox.getConverter();
        return converter != null ? converter.toString(item) : item.toString();
    }
}

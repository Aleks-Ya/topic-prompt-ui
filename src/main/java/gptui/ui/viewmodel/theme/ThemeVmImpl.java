package gptui.ui.viewmodel.theme;

import com.google.inject.Singleton;
import gptui.core.storagefilesystem.Interaction;
import gptui.core.storagefilesystem.Theme;
import gptui.ui.viewmodel.mediator.ThemeMediator;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Singleton
class ThemeVmImpl implements ThemeVmController, ThemeVmMediator {
    private static final Logger log = LoggerFactory.getLogger(ThemeVmImpl.class);
    public final ThemeVmProperties vmProperties = new ThemeVmProperties();
    private final ThemeMediator mediator;

    @Inject
    ThemeVmImpl(ThemeMediator mediator) {
        this.mediator = mediator;
    }

    @Override
    public void onThemeComboBoxAction() {
        log.trace("onThemeComboBoxAction");
        chooseThemeFromCb();
    }

    @Override
    public void onThemeFilterHistoryCheckBoxClicked() {
        log.trace("onThemeFilterHistoryCheckBoxClicked");
        var cbValue = vmProperties.filterHistoryCheckBoxSelected.getValue();
        var modelValue = mediator.isHistoryFilteringEnabled();
        log.trace("cbValue={}, modelValue={}", cbValue, modelValue);
        if (!Objects.equals(cbValue, modelValue)) {
            log.trace("Setting ThemeFilterHistoryCheckBox to {}", cbValue);
            mediator.setIsHistoryFilteringEnabled(cbValue);
            mediator.isThemeFilterHistoryChanged();
        }
    }

    @Override
    public ThemeVmProperties properties() {
        return vmProperties;
    }

    @Override
    public void addNewTheme(String theme) {
        log.trace("addNewTheme");
        var newTheme = mediator.addTheme(theme);
        mediator.setCurrentTheme(newTheme);
        mediator.themeWasChosen();
    }

    @Override
    public void renameCurrentTheme(String newTitle) {
        log.trace("renameCurrentTheme");
        var currentTheme = mediator.getCurrentTheme();
        var renamedOrTargetTheme = mediator.renameTheme(currentTheme.id(), newTitle);
        mediator.setCurrentTheme(renamedOrTargetTheme);
        mediator.themeWasChosen();
    }

    @Override
    public void updateComboBoxSelectedItemFromCurrentInteraction() {
        var themeTitle = mediator.getCurrentInteractionOpt()
                .map(Interaction::themeId)
                .map(mediator::getTheme)
                .orElse(null);
        mediator.setCurrentTheme(themeTitle);
        vmProperties.themeCbValue.setValue(themeTitle);
        updateRenameButtonDisable();
    }

    @Override
    public void updateComboBoxSelectedItemFromStateModel() {
        vmProperties.themeCbValue.setValue(mediator.getCurrentTheme());
        updateRenameButtonDisable();
    }

    private void updateRenameButtonDisable() {
        vmProperties.renameButtonDisable.setValue(mediator.getCurrentTheme() == null);
    }

    @Override
    public void updateComboBoxItems() {
        var currentModelItems = FXCollections.observableArrayList(mediator.getThemes());
        var currentComboBoxItems = vmProperties.themeCbItems.getValue();
        if (!Objects.equals(currentModelItems, currentComboBoxItems)) {
            log.trace("Set themeCbItems: {}", currentModelItems);
            vmProperties.themeCbItems.setValue(currentModelItems);
            setLabel();
        }
    }

    @Override
    public void setLabel() {
        vmProperties.themeLabelText.setValue(String.format("_Theme (%d):", mediator.getThemes().size()));
    }

    @Override
    public void initialize() {
        vmProperties.themeCbCellFactory.setValue(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Theme item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item + " (" + mediator.getInteractionCountInTheme(item.title()) + ")");
                }
            }
        });
    }

    private void chooseThemeFromCb() {
        log.trace("chooseThemeFromCb");
        var currentComboBoxValue = vmProperties.themeCbValue.getValue();
        log.trace("currentComboBoxValue: '{}'", currentComboBoxValue);
        var currentModelValue = mediator.getCurrentTheme();
        log.trace("currentModelValue: '{}'", currentModelValue);
        if (currentComboBoxValue != null && !Objects.equals(currentComboBoxValue, currentModelValue)) {
            mediator.setCurrentTheme(currentComboBoxValue);
            mediator.themeWasChosen();
        }
    }
}


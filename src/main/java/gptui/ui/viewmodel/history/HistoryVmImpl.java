package gptui.ui.viewmodel.history;

import com.google.inject.Singleton;
import gptui.core.storagefilesystem.Interaction;
import gptui.ui.viewmodel.InteractionItem;
import gptui.ui.viewmodel.mediator.HistoryMediator;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static gptui.ui.viewmodel.CbHelper.updateCbSilently;
import static java.lang.String.format;
import static javafx.collections.FXCollections.observableArrayList;

@Singleton
class HistoryVmImpl implements HistoryVmController, HistoryVmMediator {
    private static final Logger log = LoggerFactory.getLogger(HistoryVmImpl.class);
    public final HistoryVmProperties vmProperties = new HistoryVmProperties();
    private final HistoryComboBoxFacade historyCbFacade = new HistoryComboBoxFacade();
    private final StateModelFacade stateModelFacade = new StateModelFacade();
    @Inject
    private HistoryMediator mediator;

    @Override
    public void onHistoryComboBoxAction() {
        log.trace("onHistoryComboBoxAction");
        stateModelFacade.chooseHistoryCbInteractionAsCurrent();
    }

    @Override
    public void onClickHistoryDeleteButton() {
        log.trace("onClickHistoryDeleteButton");
        stateModelFacade.deleteCurrentInteraction();
        mediator.displayCurrentInteraction();
    }

    @Override
    public HistoryVmProperties properties() {
        return vmProperties;
    }

    @Override
    public void displayCurrentInteraction() {
        log.trace("displayCurrentInteraction");
        setLabel();
        historyCbFacade.setItems();
        historyCbFacade.selectCurrentInteraction();
        enableDeleteButton();
    }

    @Override
    public void selectPreviousItem() {
        log.trace("selectPreviousItem");
        historyCbFacade.selectPreviousItem();
    }

    @Override
    public void selectNextItem() {
        log.trace("selectNextItem");
        historyCbFacade.selectNextItem();
    }

    private void enableDeleteButton() {
        vmProperties.historyDeleteButtonDisable.setValue(stateModelFacade.isCurrentInteractionEmpty());
    }

    private void setLabel() {
        log.trace("setLabel");
        var historySize = stateModelFacade.getFilteredHistorySize();
        var allInteractionSize = stateModelFacade.getAllInteractionsSize();
        var label = format("Question history (%d/%d):", historySize, allInteractionSize);
        log.trace("Set label: {}", label);
        vmProperties.historyLabelText.setValue(label);
    }

    private class StateModelFacade {
        private static final Logger log = LoggerFactory.getLogger(StateModelFacade.class);

        private void chooseHistoryCbInteractionAsCurrent() {
            log.trace("chooseHistoryCbInteractionAsCurrent");
            var comboBoxCurrentInteraction = historyCbFacade.getSelectedItem();
            var modelCurrentInteraction = mediator.getCurrentInteraction();
            if (comboBoxCurrentInteraction != null && !Objects.equals(modelCurrentInteraction, comboBoxCurrentInteraction)) {
                log.debug("setCurrentInteraction from historyComboBox: {}", comboBoxCurrentInteraction.toShortString());
                mediator.setCurrentInteractionId(comboBoxCurrentInteraction.id());
                mediator.displayCurrentInteraction();
            }
        }

        private void deleteCurrentInteraction() {
            log.trace("deleteCurrentInteraction");
            mediator.deleteCurrentInteraction();
        }

        private Integer getFilteredHistorySize() {
            log.trace("getFilteredHistorySize");
            return mediator.getFilteredHistory().size();
        }

        private Integer getAllInteractionsSize() {
            log.trace("getAllInteractionsSize");
            return mediator.getFullHistory().size();
        }

        private Boolean isCurrentInteractionEmpty() {
            log.trace("isCurrentInteractionEmpty");
            return mediator.getCurrentInteractionOpt().isEmpty();
        }
    }

    private class HistoryComboBoxFacade {
        private static final Logger log = LoggerFactory.getLogger(HistoryComboBoxFacade.class);

        private Interaction getSelectedItem() {
            log.trace("getSelectedItem");
            return vmProperties.historyCbSelectionModel.getValue().getSelectedItem().interaction();
        }

        private void selectPreviousItem() {
            log.trace("selectPreviousItem");
            vmProperties.historyCbSelectionModel.getValue().selectPrevious();
        }

        private void selectNextItem() {
            log.trace("selectNextItem");
            vmProperties.historyCbSelectionModel.getValue().selectNext();
        }

        private void setItems() {
            log.trace("setItems");
            var modelItems = mediator.getFilteredHistory();
            log.trace("modelItems: {}", modelItems.size());
            var comboBoxItems = vmProperties.historyCbItems.getValue();
            log.trace("comboBoxItems: {}", comboBoxItems.size());
            var comboBoxItemInteractions = comboBoxItems.stream().map(InteractionItem::interaction).toList();
            if (!Objects.equals(modelItems, comboBoxItemInteractions)) {
                log.debug("Set items: {}", modelItems.size());
                var interactionItems = modelItems.stream()
                        .map(interaction -> new InteractionItem(mediator.getTheme(interaction.themeId()), interaction))
                        .toList();
                updateCbSilently(() -> vmProperties.historyCbItems.setValue(observableArrayList(interactionItems)),
                        vmProperties.historyCbOnAction);
            }
        }

        private void selectCurrentInteraction() {
            log.trace("selectCurrentInteraction");
            var modelCurrentInteractionIdOpt = mediator.getCurrentInteractionOpt();
            var comboBoxCurrentInteraction = vmProperties.historyCbSelectionModel.getValue().getSelectedItem();
            var cmCurrentInteraction = comboBoxCurrentInteraction != null ? comboBoxCurrentInteraction.interaction() : null;
            if (!Objects.equals(modelCurrentInteractionIdOpt.orElse(null), cmCurrentInteraction)) {
                if (modelCurrentInteractionIdOpt.isPresent()) {
                    var modelCurrentValue = modelCurrentInteractionIdOpt.get();
                    log.debug("Select interaction: '{}'", modelCurrentValue.toShortString());
                    var interactionItem = new InteractionItem(mediator.getCurrentTheme(), modelCurrentValue);
                    updateCbSilently(() -> vmProperties.historyCbSelectionModel.getValue().select(interactionItem),
                            vmProperties.historyCbOnAction);
                } else {
                    log.debug("Clear selection");
                    updateCbSilently(() -> vmProperties.historyCbSelectionModel.getValue().clearSelection(),
                            vmProperties.historyCbOnAction);
                }
            } else {
                log.debug("Selection is unchanged: '{}'", modelCurrentInteractionIdOpt.map(Interaction::toShortString));
            }
        }
    }

}


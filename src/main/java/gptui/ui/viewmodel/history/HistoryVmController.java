package gptui.ui.viewmodel.history;

public interface HistoryVmController {
    void onHistoryComboBoxAction();

    void onClickHistoryDeleteButton();

    HistoryVmProperties properties();
}

package gptui.ui.model.clipboard;

public interface ClipboardModel {
    void putHtmlToClipboard(String html);

    String getTextFromClipboard();
}

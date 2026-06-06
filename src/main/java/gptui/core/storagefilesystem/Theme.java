package gptui.core.storagefilesystem;

public record Theme(ThemeId id, String title) {
    @Override
    public String toString() {
        return title;
    }
}

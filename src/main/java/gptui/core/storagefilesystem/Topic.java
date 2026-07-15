package gptui.core.storagefilesystem;

public record Topic(TopicId id, String title) {
    @Override
    public String toString() {
        return title;
    }
}

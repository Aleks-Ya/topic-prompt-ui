package topicpromptui.core.domain;

public record Topic(TopicId id, String title) {
    @Override
    public String toString() {
        return title;
    }
}

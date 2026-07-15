package topicpromptui.core.storagefilesystem;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.UnaryOperator;

import static topicpromptui.core.util.LogUtils.shorten;

public record Interaction(InteractionId id,
                          InteractionType type,
                          TopicId topicId,
                          String question,
                          Map<AnswerType, Answer> answers,
                          InteractionId parentInteractionId) {

    public Interaction(InteractionId id, InteractionType type, TopicId topicId, String question,
                       Map<AnswerType, Answer> answers, InteractionId parentInteractionId) {
        this.id = id;
        this.type = type;
        this.topicId = topicId;
        this.question = question;
        this.answers = answers != null ? answers : new TreeMap<>();
        this.parentInteractionId = parentInteractionId;
    }

    public Optional<Answer> getAnswer(AnswerType answerType) {
        return Optional.ofNullable(answers.get(answerType));
    }

    public Interaction withAnswer(Answer answer) {
        var map = new TreeMap<>(answers);
        map.put(answer.answerType(), answer);
        return new Interaction(id, type, topicId, question, Map.copyOf(map), parentInteractionId);
    }

    public Interaction withAnswer(AnswerType answerType, UnaryOperator<Answer> update) {
        var currentAnswer = answers.getOrDefault(answerType,
                new Answer(answerType, null, null, null, null, null, null, null, null, null, null, null));
        var newAnswer = update.apply(currentAnswer);
        return withAnswer(newAnswer);
    }

    @SuppressWarnings("unused")
    public Interaction withAnswerDeleted(AnswerType answerType) {
        var map = new TreeMap<>(answers);
        map.remove(answerType);
        return new Interaction(id, type, topicId, question, Map.copyOf(map), parentInteractionId);
    }

    public Interaction withParentInteractionId(InteractionId parentInteractionId) {
        return new Interaction(id, type, topicId, question, answers, parentInteractionId);
    }

    public Interaction withTopicId(TopicId topicId) {
        return new Interaction(id, type, topicId, question, answers, parentInteractionId);
    }

    @Override
    public String toString() {
        return "Interaction{" +
                "id=" + id +
                ", type=" + type +
                ", topicId='" + topicId + '\'' +
                ", question='" + question + '\'' +
                ", answers=" + answers +
                ", parentInteractionId=" + parentInteractionId +
                '}';
    }

    public String toShortString() {
        var shortAnswers = answers.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue().toShortString()))
                .toList();
        return "Interaction{" +
                "id=" + id +
                ", type=" + type +
                ", topicId='" + topicId + '\'' +
                ", question='" + shorten(question) + '\'' +
                ", answers=" + shortAnswers +
                '}';
    }
}

package topicpromptui.core.ai.grader;

public record Score(int score) {
    public static Score MIN = new Score(0);
    public static Score MAX = new Score(10);

    public Score combine(Score newScore) {
        return new Score((this.score + newScore.score) / 2);
    }

}

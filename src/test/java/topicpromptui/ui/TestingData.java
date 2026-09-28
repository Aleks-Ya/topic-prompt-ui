package topicpromptui.ui;

import topicpromptui.core.domain.Answer;
import topicpromptui.core.domain.Interaction;
import topicpromptui.core.domain.InteractionId;
import topicpromptui.core.domain.InteractionType;
import topicpromptui.core.domain.Topic;
import topicpromptui.core.domain.TopicId;

import java.util.List;
import java.util.Map;

import static topicpromptui.core.domain.AnswerState.FAIL;
import static topicpromptui.core.domain.AnswerState.SUCCESS;
import static topicpromptui.core.domain.AnswerType.AI_2;
import static topicpromptui.core.domain.AnswerType.AI_3;
import static topicpromptui.core.domain.AnswerType.GRAMMAR;
import static topicpromptui.core.domain.AnswerType.AI_1;

public class TestingData {
    public static final String GRAMMAR_CORRECT = "Correct";

    public static class I0 {
        public static final String QUESTION = "";
        public static final String GRAMMAR_HTML = "";
        public static final String AI_1_HTML = "";
        public static final String AI_2_HTML = "";
        public static final String AI_3_HTML = "";
        public static final List<Interaction> HISTORY_ITEMS = List.of();
        public static final Interaction HISTORY_SELECTED_ITEM = null;
        public static final Topic TOPIC_SELECTED_ITEM = null;
        public static final int TOPIC_SIZE = 0;
        public static final Topic[] TOPIC_ITEMS = new Topic[]{};

    }

    public static class I1 {
        public static final TopicId TOPIC_ID = new TopicId(1L);
        public static final Topic TOPIC = new Topic(TOPIC_ID, "Topic 1");
        public static final String QUESTION = "Question 1";
        public static final String GRAMMAR_HTML = "Grammar answer HTML 1";
        public static final String AI_1_HTML = "AI_1 answer HTML 1";
        public static final String AI_2_HTML = "AI_2 answer HTML 1";
        public static final String AI_3_HTML = "AI_3 answer HTML 1";
        /** What the grammar check answers when the question has no mistakes: the question itself, unchanged. */
        public static final String GRAMMAR_ANSWER = QUESTION;
        public static final String EXP_GRAMMAR_ANSWER_BODY = wrapExpectedWebViewContent(GRAMMAR_CORRECT);
        public static final String EXP_AI_1_HTML_BODY = wrapExpectedWebViewContent(AI_1_HTML);
        public static final String EXP_AI_2_HTML_BODY = wrapExpectedWebViewContent(AI_2_HTML);
        public static final String EXP_AI_3_HTML_BODY = wrapExpectedWebViewContent(AI_3_HTML);
        public static final Interaction INTERACTION = new Interaction(new InteractionId(1L), InteractionType.QUESTION,
                TOPIC_ID, QUESTION, Map.of(
                GRAMMAR, new Answer(GRAMMAR, "QC prompt 1", "Grammar answer MD 1", GRAMMAR_HTML, SUCCESS, null, null, null, null, null, null, null),
                AI_1, new Answer(AI_1, "AI_1 prompt 1", "AI_1 answer MD 1", AI_1_HTML, SUCCESS, null, null, null, null, null, null, null),
                AI_2, new Answer(AI_2, "AI_2 prompt 1", "AI_2 answer MD 1", AI_2_HTML, SUCCESS, null, null, null, null, null, null, null),
                AI_3, new Answer(AI_3, "AI_3 prompt 1", "AI_3 answer MD 1", AI_3_HTML, SUCCESS, null, null, null, null, null, null, null)), null);
    }

    public static class I2 {
        public static final TopicId TOPIC_ID = new TopicId(2L);
        public static final Topic TOPIC = new Topic(TOPIC_ID, "Topic 2");
        public static final String QUESTION = "Question 2";
        public static final String GRAMMAR_HTML = "Grammar answer HTML 2";
        public static final String AI_1_HTML = "AI_1 answer HTML 2";
        public static final String AI_2_HTML = "AI_2 answer HTML 2".repeat(AI_2_ANSWER_MULTIPLIER);
        public static final String AI_3_HTML = "AI_3 answer HTML 2";
        /** What the grammar check answers when the question has no mistakes: the question itself, unchanged. */
        public static final String GRAMMAR_ANSWER = QUESTION;
        public static final String EXP_GRAMMAR_ANSWER_BODY = wrapExpectedWebViewContent(GRAMMAR_CORRECT);
        public static final String EXP_AI_1_HTML_BODY = wrapExpectedWebViewContent(I2.AI_1_HTML);
        public static final String EXP_AI_2_HTML_BODY = wrapExpectedWebViewContent(I2.AI_2_HTML);
        public static final String EXP_AI_3_HTML_BODY = wrapExpectedWebViewContent(I2.AI_3_HTML);
        public static final Interaction INTERACTION = new Interaction(new InteractionId(2L), InteractionType.QUESTION,
                TOPIC_ID, QUESTION, Map.of(
                GRAMMAR, new Answer(GRAMMAR, "QC prompt 2", "Grammar answer MD 2", I2.GRAMMAR_HTML, SUCCESS, null, null, null, null, null, null, null),
                AI_1, new Answer(AI_1, "AI_1 prompt 2", "AI_1 answer MD 2", I2.AI_1_HTML, SUCCESS, null, null, null, null, null, null, null),
                AI_2, new Answer(AI_2, "AI_2 prompt 2", "AI_2 answer MD 2".repeat(AI_2_ANSWER_MULTIPLIER), I2.AI_2_HTML, FAIL, null, null, null, null, null, null, null),
                AI_3, new Answer(AI_3, "AI_3 prompt 2", "AI_3 answer MD 2", I2.AI_3_HTML, SUCCESS, null, null, null, null, null, null, null)), null);
    }

    public static class I3 {
        public static final TopicId TOPIC_ID = new TopicId(3L);
        public static final Topic TOPIC = new Topic(TOPIC_ID, "Topic 3");
        public static final String QUESTION = "Question 3";
        public static final String GRAMMAR_HTML = "Grammar answer HTML 3";
        public static final String AI_1_HTML = "AI_1 answer HTML 3";
        public static final String AI_2_HTML = "AI_2 answer HTML 3".repeat(AI_2_ANSWER_MULTIPLIER);
        public static final String AI_3_HTML = "AI_3 answer HTML 3";
        public static final String EXP_AI_1_HTML_BODY = wrapExpectedWebViewContent(I3.AI_1_HTML);
        public static final String EXP_AI_2_HTML_BODY = wrapExpectedWebViewContent(I3.AI_2_HTML);
        public static final String EXP_AI_3_HTML_BODY = wrapExpectedWebViewContent(I3.AI_3_HTML);
        public static final Interaction INTERACTION = new Interaction(new InteractionId(3L), InteractionType.QUESTION,
                TOPIC_ID, QUESTION, Map.of(
                GRAMMAR, new Answer(GRAMMAR, "QC prompt 3", "Grammar answer MD 3", I3.GRAMMAR_HTML, SUCCESS, null, null, null, null, null, null, null),
                AI_1, new Answer(AI_1, "AI_1 prompt 3", "AI_1 answer MD 3", I3.AI_1_HTML, SUCCESS, null, null, null, null, null, null, null),
                AI_2, new Answer(AI_2, "AI_2 prompt 3", "AI_2 answer MD 3".repeat(AI_2_ANSWER_MULTIPLIER), I3.AI_2_HTML, FAIL, null, null, null, null, null, null, null),
                AI_3, new Answer(AI_3, "AI_3 prompt 3", "AI_3 answer MD 3", I3.AI_3_HTML, SUCCESS, null, null, null, null, null, null, null)), null);
    }

    private static final int AI_2_ANSWER_MULTIPLIER = 150;

    private static String wrapExpectedWebViewContent(String text) {
        return "<p>" + text + "</p>\n";
    }
}

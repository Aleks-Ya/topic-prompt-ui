package topicpromptui.ui;

import topicpromptui.core.storagefilesystem.Answer;
import topicpromptui.core.storagefilesystem.Interaction;
import topicpromptui.core.storagefilesystem.InteractionId;
import topicpromptui.core.storagefilesystem.InteractionType;
import topicpromptui.core.storagefilesystem.Topic;
import topicpromptui.core.storagefilesystem.TopicId;

import java.util.List;
import java.util.Map;

import static topicpromptui.core.storagefilesystem.AnswerState.FAIL;
import static topicpromptui.core.storagefilesystem.AnswerState.SUCCESS;
import static topicpromptui.core.storagefilesystem.AnswerType.CLAUDE;
import static topicpromptui.core.storagefilesystem.AnswerType.GCP;
import static topicpromptui.core.storagefilesystem.AnswerType.GRAMMAR;
import static topicpromptui.core.storagefilesystem.AnswerType.OPEN_AI;

public class TestingData {
    public static class I0 {
        public static final String QUESTION = "";
        public static final String GRAMMAR_HTML = "";
        public static final String OPEN_AI_HTML = "";
        public static final String CLAUDE_HTML = "";
        public static final String GCP_HTML = "";
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
        public static final String OPEN_AI_HTML = "OpenAI answer HTML 1";
        public static final String CLAUDE_HTML = "Claude answer HTML 1";
        public static final String GCP_HTML = "GCP answer HTML 1";
        public static final String EXP_GRAMMAR_HTML_BODY = wrapExpectedWebViewContent(GRAMMAR_HTML);
        public static final String EXP_OPEN_AI_HTML_BODY = wrapExpectedWebViewContent(OPEN_AI_HTML);
        public static final String EXP_CLAUDE_HTML_BODY = wrapExpectedWebViewContent(CLAUDE_HTML);
        public static final String EXP_GCP_HTML_BODY = wrapExpectedWebViewContent(GCP_HTML);
        public static final Interaction INTERACTION = new Interaction(new InteractionId(1L), InteractionType.QUESTION,
                TOPIC_ID, QUESTION, Map.of(
                GRAMMAR, new Answer(GRAMMAR, "QC prompt 1", "Grammar answer MD 1", GRAMMAR_HTML, SUCCESS, null, null, null, null, null, null, null),
                OPEN_AI, new Answer(OPEN_AI, "OpenAI prompt 1", "OpenAI answer MD 1", OPEN_AI_HTML, SUCCESS, null, null, null, null, null, null, null),
                CLAUDE, new Answer(CLAUDE, "Claude prompt 1", "Claude answer MD 1", CLAUDE_HTML, SUCCESS, null, null, null, null, null, null, null),
                GCP, new Answer(GCP, "GCP prompt 1", "GCP answer MD 1", GCP_HTML, SUCCESS, null, null, null, null, null, null, null)), null);
    }

    public static class I2 {
        public static final TopicId TOPIC_ID = new TopicId(2L);
        public static final Topic TOPIC = new Topic(TOPIC_ID, "Topic 2");
        public static final String QUESTION = "Question 2";
        public static final String GRAMMAR_HTML = "Grammar answer HTML 2";
        public static final String OPEN_AI_HTML = "OpenAI answer HTML 2";
        public static final String CLAUDE_HTML = "Claude answer HTML 2".repeat(CLAUDE_ANSWER_MULTIPLIER);
        public static final String GCP_HTML = "GCP answer HTML 2";
        public static final String EXP_GRAMMAR_HTML_BODY = wrapExpectedWebViewContent(I2.GRAMMAR_HTML);
        public static final String EXP_OPEN_AI_HTML_BODY = wrapExpectedWebViewContent(I2.OPEN_AI_HTML);
        public static final String EXP_CLAUDE_HTML_BODY = wrapExpectedWebViewContent(I2.CLAUDE_HTML);
        public static final String EXP_GCP_HTML_BODY = wrapExpectedWebViewContent(I2.GCP_HTML);
        public static final Interaction INTERACTION = new Interaction(new InteractionId(2L), InteractionType.QUESTION,
                TOPIC_ID, QUESTION, Map.of(
                GRAMMAR, new Answer(GRAMMAR, "QC prompt 2", "Grammar answer MD 2", I2.GRAMMAR_HTML, SUCCESS, null, null, null, null, null, null, null),
                OPEN_AI, new Answer(OPEN_AI, "OpenAI prompt 2", "OpenAI answer MD 2", I2.OPEN_AI_HTML, SUCCESS, null, null, null, null, null, null, null),
                CLAUDE, new Answer(CLAUDE, "Claude prompt 2", "Claude answer MD 2".repeat(CLAUDE_ANSWER_MULTIPLIER), I2.CLAUDE_HTML, FAIL, null, null, null, null, null, null, null),
                GCP, new Answer(GCP, "GCP prompt 2", "GCP answer MD 2", I2.GCP_HTML, SUCCESS, null, null, null, null, null, null, null)), null);
    }

    public static class I3 {
        public static final TopicId TOPIC_ID = new TopicId(3L);
        public static final Topic TOPIC = new Topic(TOPIC_ID, "Topic 3");
        public static final String QUESTION = "Question 3";
        public static final String GRAMMAR_HTML = "Grammar answer HTML 3";
        public static final String OPEN_AI_HTML = "OpenAI answer HTML 3";
        public static final String CLAUDE_HTML = "Claude answer HTML 3".repeat(CLAUDE_ANSWER_MULTIPLIER);
        public static final String GCP_HTML = "GCP answer HTML 3";
        public static final String EXP_GRAMMAR_HTML_BODY = wrapExpectedWebViewContent(I3.GRAMMAR_HTML);
        public static final String EXP_OPEN_AI_HTML_BODY = wrapExpectedWebViewContent(I3.OPEN_AI_HTML);
        public static final String EXP_CLAUDE_HTML_BODY = wrapExpectedWebViewContent(I3.CLAUDE_HTML);
        public static final String EXP_GCP_HTML_BODY = wrapExpectedWebViewContent(I3.GCP_HTML);
        public static final Interaction INTERACTION = new Interaction(new InteractionId(3L), InteractionType.QUESTION,
                TOPIC_ID, QUESTION, Map.of(
                GRAMMAR, new Answer(GRAMMAR, "QC prompt 3", "Grammar answer MD 3", I3.GRAMMAR_HTML, SUCCESS, null, null, null, null, null, null, null),
                OPEN_AI, new Answer(OPEN_AI, "OpenAI prompt 3", "OpenAI answer MD 3", I3.OPEN_AI_HTML, SUCCESS, null, null, null, null, null, null, null),
                CLAUDE, new Answer(CLAUDE, "Claude prompt 3", "Claude answer MD 3".repeat(CLAUDE_ANSWER_MULTIPLIER), I3.CLAUDE_HTML, FAIL, null, null, null, null, null, null, null),
                GCP, new Answer(GCP, "GCP prompt 3", "GCP answer MD 3", I3.GCP_HTML, SUCCESS, null, null, null, null, null, null, null)), null);
    }

    private static final int CLAUDE_ANSWER_MULTIPLIER = 150;

    private static String wrapExpectedWebViewContent(String text) {
        return "<p>" + text + "</p>\n";
    }
}

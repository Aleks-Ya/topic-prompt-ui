package gptui.core.util;

import org.slf4j.MDC;

public class Mdc {
    private static final String INTERACTION_ID_MDC = "interactionId";
    private static final String ANSWER_TYPE_MDC = "answerType";

    private Mdc() {
    }

    public static void run(long interactionId, Runnable runnable) {
        MDC.put(INTERACTION_ID_MDC, interactionId + " ");
        runnable.run();
        MDC.remove(INTERACTION_ID_MDC);
    }

    public static void run(String answerType, Runnable runnable) {
        MDC.put(ANSWER_TYPE_MDC, " " + answerType);
        runnable.run();
        MDC.remove(ANSWER_TYPE_MDC);
    }
}

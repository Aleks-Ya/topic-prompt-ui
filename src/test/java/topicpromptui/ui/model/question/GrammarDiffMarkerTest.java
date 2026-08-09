package topicpromptui.ui.model.question;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import topicpromptui.BaseTest;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class GrammarDiffMarkerTest extends BaseTest {
    private final GrammarDiffMarker grammarDiffMarker = injector.getInstance(GrammarDiffMarker.class);

    static Stream<Arguments> markChangesCases() {
        return Stream.of(
                Arguments.of("noChanges",
                        "What is the difference between a thread and a process?",
                        "What is the difference between a thread and a process?",
                        "What is the difference between a thread and a process?"),
                Arguments.of("correctSentinel",
                        "What is the latest Java version?",
                        "Correct",
                        "Correct"),
                Arguments.of("insertedWord",
                        "What's latest Java version?",
                        "What's the latest Java version?",
                        "What's **the** latest Java version?"),
                Arguments.of("replacedWord",
                        "Garbaj collector",
                        "Garbage collector",
                        "**Garbage** collector"),
                Arguments.of("replacedFirstAndLastWord",
                        "Garbaj collectr",
                        "Garbage collector",
                        "**Garbage collector**"),
                Arguments.of("twoSeparateReplacements",
                        "He go to the office yesterday and speak with the team.",
                        "He went to the office yesterday and spoke with the team.",
                        "He **went** to the office yesterday and **spoke** with the team."),
                Arguments.of("removedWordInTheMiddle",
                        "I discussed about the problem with the team.",
                        "I discussed the problem with the team.",
                        "I **discussed the** problem with the team."),
                Arguments.of("removedFirstWord",
                        "The what is a thread pool?",
                        "What is a thread pool?",
                        "**What** is a thread pool?"),
                Arguments.of("removedLastWord",
                        "What is a thread pool for?",
                        "What is a thread pool?",
                        "What is a thread **pool?**"),
                Arguments.of("modelStillBoldsItsOwnChanges",
                        "What's latest Java version?",
                        "What's **the latest** Java version?",
                        "What's **the** latest Java version?"),
                Arguments.of("multiLineQuestionKeepsLineBreaks",
                        """
                                What is a thread pool?
                                And why I need it?""",
                        """
                                What is a thread pool?
                                And why do I need it?""",
                        """
                                What is a thread pool?
                                And why **do** I need it?"""),
                // A bold span cannot straddle a line break, so neighbouring changes on two lines stay separate
                Arguments.of("changesOnBothSidesOfALineBreak",
                        """
                                What is thread pool
                                a queue?""",
                        """
                                What is a thread pool
                                or a queue?""",
                        """
                                What is **a** thread pool
                                **or** a queue?"""),
                Arguments.of("blankQuestion", "  ", "Whatever the model said", "Whatever the model said"),
                Arguments.of("blankAnswer", "What is a thread pool?", "", ""),
                Arguments.of("nullAnswer", "What is a thread pool?", null, null)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("markChangesCases")
    void markChanges(String caseName, String question, String correctedMd, String expected) {
        assertThat(grammarDiffMarker.markChanges(question, correctedMd)).isEqualTo(expected);
    }
}

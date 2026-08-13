package topicpromptui.ui.question;

import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.ui.TestingData.I1;
import javafx.scene.layout.Region;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import org.junit.jupiter.api.Test;

import static topicpromptui.core.domain.AnswerType.OPEN_AI;
import static javafx.scene.paint.Color.LIGHTBLUE;
import static topicpromptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EMPTY;
import static topicpromptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_FOLLOW_UP;
import static java.time.Duration.ZERO;
import static org.assertj.core.api.Assertions.assertThat;

class FollowUpQuestionUiTest extends BaseTopicPromptUiTest {
    @Override
    public void init() {
        storage.saveTopic(I1.TOPIC);
        storage.saveInteraction(I1.INTERACTION);
    }

    @Test
    void followUpCheckboxLinksNewInteractionAndSendsHistory() {
        gptApi.clear().putGrammarResponse("Correct", ZERO).putResponse("Who created it?", "James Gosling created it.", ZERO);
        claudeApi.clear().putResponse("Who created it?", "James Gosling created it.", ZERO);
        gcpApi.clear().putResponse("Who created it?", "James Gosling created it.", ZERO);

        clickOn(question().followUpCheckBox());
        clickOn(question().textArea());
        overWrite("Who created it?");
        clickOn(question().questionButton());

        gptApi.waitUntilSent(2);
        claudeApi.waitUntilSent(1);
        gcpApi.waitUntilSent(1);

        var followUpInteractionId = stateModel.getCurrentInteractionId();
        var followUpInteraction = storage.readInteraction(followUpInteractionId).orElseThrow();
        assertThat(followUpInteraction.parentInteractionId()).isEqualTo(I1.INTERACTION.id());

        var openAiAncestorAnswer = I1.INTERACTION.getAnswer(OPEN_AI).orElseThrow();
        var turns = gptApi.getTurnsSendHistory().stream().filter(t -> t.size() > 1).findFirst().orElseThrow();
        assertThat(turns).hasSize(3);
        assertThat(turns.get(0).content()).isEqualTo(openAiAncestorAnswer.prompt());
        assertThat(turns.get(1).content()).isEqualTo(openAiAncestorAnswer.answerMd());
        assertThat(turns.get(2).content()).isEqualTo("Who created it?");

        assertThat(claudeApi.getTurnsSendHistory().getLast()).hasSize(3);
        assertThat(gcpApi.getTurnsSendHistory().getLast()).hasSize(3);
    }

    @Test
    void followUpCheckboxTurnsQuestionBackgroundLightBlue() {
        assertThat(question().textArea().getStyleClass())
                .contains(QUESTION_STYLE_EMPTY).doesNotContain(QUESTION_STYLE_FOLLOW_UP);

        clickOn(question().followUpCheckBox());
        assertThat(question().textArea().getStyleClass())
                .contains(QUESTION_STYLE_FOLLOW_UP).doesNotContain(QUESTION_STYLE_EMPTY);
        // The rendered colour, not just the class: proves app.css was found and its rule applied.
        assertThat(questionTextAreaBackground()).isEqualTo(LIGHTBLUE);

        clickOn(question().followUpCheckBox());
        assertThat(question().textArea().getStyleClass())
                .contains(QUESTION_STYLE_EMPTY).doesNotContain(QUESTION_STYLE_FOLLOW_UP);
    }

    // Modena renders -fx-control-inner-background as a subtle top-down gradient whose bottom stop is the
    // colour itself, so the plain Color only shows up there.
    private Paint questionTextAreaBackground() {
        var content = (Region) question().textArea().lookup(".content");
        var fill = content.getBackground().getFills().getFirst().getFill();
        return fill instanceof LinearGradient gradient ? gradient.getStops().getLast().getColor() : fill;
    }
}

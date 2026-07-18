package topicpromptui.ui.model.state;

import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.ui.TestingData.I1;
import topicpromptui.ui.TestingData.I2;
import topicpromptui.ui.TestingData.I3;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HistoryFilterTextTest extends BaseTopicPromptUiTest {
    @Override
    public void init() {
        storage.saveTopic(I1.TOPIC);
        storage.saveTopic(I2.TOPIC);
        storage.saveTopic(I3.TOPIC);
        storage.saveInteraction(I1.INTERACTION);
        storage.saveInteraction(I2.INTERACTION);
        storage.saveInteraction(I3.INTERACTION);
    }

    @Test
    void emptyFilterKeepsFullHistory() {
        assertThat(stateModel.getHistoryFilterText()).isEmpty();
        assertThat(stateModel.getFilteredHistory())
                .containsExactly(I3.INTERACTION, I2.INTERACTION, I1.INTERACTION);
    }

    @Test
    void blankFilterKeepsFullHistory() {
        stateModel.setHistoryFilterText("  ");
        assertThat(stateModel.getFilteredHistory())
                .containsExactly(I3.INTERACTION, I2.INTERACTION, I1.INTERACTION);
    }

    @Test
    void filterMatchesQuestionSubstringCaseInsensitively() {
        stateModel.setHistoryFilterText("qUeStIoN 1");
        assertThat(stateModel.getFilteredHistory()).containsExactly(I1.INTERACTION);
    }

    @Test
    void filterWithoutMatchesLeavesHistoryEmpty() {
        stateModel.setHistoryFilterText("no such question");
        assertThat(stateModel.getFilteredHistory()).isEmpty();
    }

    @Test
    void filterMatchesQuestionOnlyNotTopicTitle() {
        stateModel.setHistoryFilterText(I1.TOPIC.title());
        assertThat(stateModel.getFilteredHistory()).isEmpty();
    }

    @Test
    void filterCombinesWithTopicFilteringUsingAnd() {
        stateModel.setCurrentTopic(I3.TOPIC);
        stateModel.setIsHistoryFilteringEnabled(true);
        stateModel.setHistoryFilterText("question");
        assertThat(stateModel.getFilteredHistory()).containsExactly(I3.INTERACTION);
        stateModel.setHistoryFilterText("1");
        assertThat(stateModel.getFilteredHistory()).isEmpty();
    }

    @Test
    void nullFilterIsNormalizedToEmpty() {
        stateModel.setHistoryFilterText(null);
        assertThat(stateModel.getHistoryFilterText()).isEmpty();
        assertThat(stateModel.getFilteredHistory())
                .containsExactly(I3.INTERACTION, I2.INTERACTION, I1.INTERACTION);
    }
}

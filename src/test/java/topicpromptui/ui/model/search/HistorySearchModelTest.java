package topicpromptui.ui.model.search;

import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.ui.TestingData.I1;
import topicpromptui.ui.TestingData.I2;
import topicpromptui.ui.TestingData.I3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HistorySearchModelTest extends BaseTopicPromptUiTest {
    @Override
    public void init() {
        storage.saveTopic(I1.TOPIC);
        storage.saveTopic(I2.TOPIC);
        storage.saveTopic(I3.TOPIC);
    }

    @Test
    void searchEmptyIndex() {
        assertThat(search.search("table")).isEmpty();
    }

    @Test
    void indexDocument() {
        assertThat(search.search("topic")).isEmpty();
        search.indexDocument(I1.INTERACTION);
        assertThat(search.search("topic")).containsExactly(I1.INTERACTION.id());
    }

    @Test
    void indexDocuments() {
        assertThat(search.search("topic")).isEmpty();
        search.indexDocuments(List.of(I1.INTERACTION, I2.INTERACTION, I3.INTERACTION));
        assertThat(search.search("topic"))
                .containsExactly(I1.INTERACTION.id(), I2.INTERACTION.id(), I3.INTERACTION.id());
    }

    @Test
    void search() {
        search.indexDocuments(List.of(I1.INTERACTION, I2.INTERACTION, I3.INTERACTION));
        assertThat(search.search("absent")).isEmpty();
        assertThat(search.search("topic"))
                .containsExactly(I1.INTERACTION.id(), I2.INTERACTION.id(), I3.INTERACTION.id());
        assertThat(search.search("Topics"))
                .containsExactly(I1.INTERACTION.id(), I2.INTERACTION.id(), I3.INTERACTION.id());
        assertThat(search.search("question"))
                .containsExactly(I1.INTERACTION.id(), I2.INTERACTION.id(), I3.INTERACTION.id());
        assertThat(search.search("Questions"))
                .containsExactly(I1.INTERACTION.id(), I2.INTERACTION.id(), I3.INTERACTION.id());
    }
}
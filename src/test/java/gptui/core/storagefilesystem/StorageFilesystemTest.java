package gptui.core.storagefilesystem;

import gptui.BaseTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static gptui.core.storagefilesystem.AnswerState.SUCCESS;
import static gptui.core.storagefilesystem.AnswerType.OPEN_AI;
import static gptui.core.storagefilesystem.InteractionType.QUESTION;
import static org.assertj.core.api.Assertions.assertThat;

class StorageFilesystemTest extends BaseTest {
    private final StorageFilesystem storageFileSystem = injector.getInstance(StorageFilesystem.class);

    @Test
    void readAllInteractions() {
        var interactions = storageFileSystem.readAllInteractions();
        assertThat(interactions).isEmpty();
    }

    @Test
    void saveInteraction() {
        assertThat(storageFileSystem.readAllInteractions()).isEmpty();
        var interaction1 = new Interaction(new InteractionId(1L), QUESTION, new ThemeId(1L), "question1", null, null);
        var interaction2 = new Interaction(new InteractionId(2L), QUESTION, new ThemeId(2L), "question2", null, null);
        storageFileSystem.saveInteraction(interaction1);
        storageFileSystem.saveInteraction(interaction2);
        assertThat(storageFileSystem.readAllInteractions()).containsExactlyInAnyOrder(interaction1, interaction2);
    }

    @Test
    void saveInteractionWithParentAndResponseId() {
        assertThat(storageFileSystem.readAllInteractions()).isEmpty();
        var parent = new Interaction(new InteractionId(1L), QUESTION, new ThemeId(1L), "question1", null, null);
        var followUp = new Interaction(new InteractionId(2L), QUESTION, new ThemeId(1L), "question2", Map.of(
                OPEN_AI, new Answer(OPEN_AI, "prompt2", "answerMd2", "answerHtml2", SUCCESS, "resp_123",
                        null, null, null, null, null, null)),
                parent.id());
        storageFileSystem.saveInteraction(parent);
        storageFileSystem.saveInteraction(followUp);
        assertThat(storageFileSystem.readAllInteractions()).containsExactlyInAnyOrder(parent, followUp);
    }

    @Test
    void deleteInteraction() {
        assertThat(storageFileSystem.readAllInteractions()).isEmpty();
        var interaction1 = new Interaction(new InteractionId(1L), QUESTION, new ThemeId(1L), "question1", null, null);
        var interaction2 = new Interaction(new InteractionId(2L), QUESTION, new ThemeId(2L), "question2", null, null);
        storageFileSystem.saveInteraction(interaction1);
        storageFileSystem.saveInteraction(interaction2);
        assertThat(storageFileSystem.readAllInteractions()).containsExactlyInAnyOrder(interaction1, interaction2);

        storageFileSystem.deleteInteraction(interaction1.id());
        assertThat(storageFileSystem.readAllInteractions()).containsExactlyInAnyOrder(interaction2);
    }

    @Test
    void saveReadThemes() {
        assertThat(storageFileSystem.readThemes()).isEmpty();
        var theme1 = new Theme(new ThemeId(1L), "Java");
        var theme2 = new Theme(new ThemeId(2L), "Scala");
        var themes = List.of(theme1, theme2);
        storageFileSystem.saveThemes(themes);
        assertThat(storageFileSystem.readThemes()).containsExactly(theme1, theme2);
    }

}
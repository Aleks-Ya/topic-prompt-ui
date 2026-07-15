package gptui.ui.model.storage;

import gptui.BaseTest;
import gptui.core.storagefilesystem.Interaction;
import gptui.core.storagefilesystem.InteractionId;
import gptui.core.storagefilesystem.StorageFilesystemImpl;
import gptui.core.storagefilesystem.Topic;
import gptui.core.storagefilesystem.TopicId;
import gptui.ui.TestingData.I1;
import gptui.ui.TestingData.I2;
import gptui.ui.TestingData.I3;
import gptui.core.config.ConfigModel;
import org.junit.jupiter.api.Test;

import static gptui.core.storagefilesystem.AnswerType.GRAMMAR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StorageModelTest extends BaseTest {
    private final ConfigModel configModel = injector.getInstance(ConfigModel.class);
    private final StorageModel storage = injector.getInstance(StorageModel.class);

    @Test
    void newInteractionId() {
        assertThat(storage.newInteractionId()).isNotNull();
    }

    @Test
    void updateInteraction() {
        assertThat(storage.readInteraction(I1.INTERACTION.id())).isEmpty();
        storage.saveTopic(I1.TOPIC);
        storage.saveInteraction(I1.INTERACTION);
        assertThat(storage.readInteraction(I1.INTERACTION.id())).contains(I1.INTERACTION);
        var newGrammarPrompt = "new GRAMMAR prompt";
        storage.updateInteraction(I1.INTERACTION.id(), i -> i.withAnswer(GRAMMAR, answer -> answer.withPrompt(newGrammarPrompt)));
        assertThat(storage.readInteraction(I1.INTERACTION.id()))
                .map(interaction -> interaction.getAnswer(GRAMMAR))
                .map(answer -> answer.orElseThrow().prompt())
                .contains(newGrammarPrompt);
    }

    @Test
    void saveInteraction() {
        assertThat(storage.readAllInteractions()).isEmpty();
        storage.saveTopic(I1.TOPIC);
        storage.saveTopic(I2.TOPIC);
        storage.saveInteraction(I1.INTERACTION);
        storage.saveInteraction(I2.INTERACTION);
        assertThat(storage.readAllInteractions()).containsExactlyInAnyOrder(I1.INTERACTION, I2.INTERACTION);
    }

    @Test
    void readInteraction() {
        var interactionId = new InteractionId(1L);
        assertThat(storage.readInteraction(interactionId)).isEmpty();
        storage.saveTopic(I1.TOPIC);
        storage.saveInteraction(I1.INTERACTION);
        assertThat(storage.readInteraction(I1.INTERACTION.id())).contains(I1.INTERACTION);
    }

    @Test
    void readNullInteraction() {
        assertThat(storage.readInteraction(null)).isEmpty();
    }

    @Test
    void readAllInteractions() {
        assertThat(storage.readAllInteractions()).isEmpty();
        storage.saveTopic(I1.TOPIC);
        storage.saveTopic(I2.TOPIC);
        storage.saveTopic(I3.TOPIC);
        storage.saveInteraction(I1.INTERACTION);
        storage.saveInteraction(I2.INTERACTION);
        storage.saveInteraction(I3.INTERACTION);
        assertThat(storage.readAllInteractions()).containsExactlyInAnyOrder(I1.INTERACTION, I2.INTERACTION, I3.INTERACTION);
    }

    @Test
    void deleteInteraction() {
        assertThat(storage.readAllInteractions()).isEmpty();
        storage.saveTopic(I1.TOPIC);
        storage.saveTopic(I2.TOPIC);
        storage.saveInteraction(I1.INTERACTION);
        storage.saveInteraction(I2.INTERACTION);
        assertThat(storage.readAllInteractions()).containsExactlyInAnyOrder(I1.INTERACTION, I2.INTERACTION);

        storage.deleteInteraction(I1.INTERACTION.id());
        assertThat(storage.readAllInteractions()).containsExactlyInAnyOrder(I2.INTERACTION);
    }

    @Test
    void getTopicsSeveral() {
        var topicTitle1 = "AAA";
        var topicTitle2 = "BBB";
        var topicTitle4 = "CCC";
        var topic1 = new Topic(new TopicId(1L), topicTitle1);
        var topic2 = new Topic(new TopicId(2L), topicTitle2);
        var topic4 = new Topic(new TopicId(4L), topicTitle4);
        var id1 = 1693929900L;
        var id2 = id1 - 1;
        var id3 = id1 + 1;
        var id4 = id1 + 2;
        var id5 = id1 - 2;
        storage.saveTopic(topic1);
        storage.saveTopic(topic2);
        storage.saveTopic(topic4);
        storage.saveInteraction(newInteraction(id1, topic1));
        storage.saveInteraction(newInteraction(id2, topic2));
        storage.saveInteraction(newInteraction(id3, topic2));
        storage.saveInteraction(newInteraction(id4, topic4));
        storage.saveInteraction(newInteraction(id5, topic2));
        assertThat(storage.getTopics().stream().map(Topic::title)).containsExactly(topicTitle2, topicTitle4, topicTitle1);
    }

    @Test
    void getTopicsSingle() {
        var topicTitle = "AAA";
        var topic = new Topic(new TopicId(1L), topicTitle);
        storage.saveTopic(topic);
        storage.saveInteraction(newInteraction(1693929900L, topic));
        assertThat(storage.getTopics().stream().map(Topic::title)).containsExactly(topicTitle);
    }

    @Test
    void getTopicsEmpty() {
        assertThat(storage.getTopics()).isEmpty();
    }

    @Test
    void getTopicsSeveralStart() {
        var storage1 = new StorageModelImpl(new StorageFilesystemImpl(configModel));
        var topicTitle1 = "AAA";
        var topicTitle2 = "BBB";
        var topicTitle4 = "CCC";
        var topic1 = new Topic(new TopicId(1L), topicTitle1);
        var topic2 = new Topic(new TopicId(2L), topicTitle2);
        var topic4 = new Topic(new TopicId(4L), topicTitle4);
        storage1.saveTopic(topic1);
        storage1.saveTopic(topic2);
        storage1.saveTopic(topic4);
        var id1 = 1693929900L;
        var id2 = id1 - 1;
        var id3 = id1 + 1;
        var id4 = id1 + 2;
        var id5 = id1 - 2;
        storage1.saveInteraction(newInteraction(id1, topic1));
        storage1.saveInteraction(newInteraction(id2, topic2));
        storage1.saveInteraction(newInteraction(id3, topic2));
        storage1.saveInteraction(newInteraction(id4, topic4));
        storage1.saveInteraction(newInteraction(id5, topic2));

        var storage2 = new StorageModelImpl(new StorageFilesystemImpl(configModel));
        assertThat(storage2.getTopics().stream().map(Topic::title)).containsExactly(topicTitle4, topicTitle2, topicTitle1);
    }

    @Test
    void getTopicsSingleStart() {
        var storage1 = new StorageModelImpl(new StorageFilesystemImpl(configModel));
        var topicTitle = "AAA";
        var topic = new Topic(new TopicId(1L), topicTitle);
        storage1.saveTopic(topic);
        storage1.saveInteraction(newInteraction(1693929900L, topic));

        var storage2 = new StorageModelImpl(new StorageFilesystemImpl(configModel));
        assertThat(storage2.getTopics()).containsExactly(topic);
    }

    @Test
    void getTopicsEmptyStart() {
        assertThat(storage.getTopics()).isEmpty();
    }

    @Test
    void addTopic() {
        var title = "Java";
        var topic1 = storage.addTopic(title);
        assertThat(topic1).isEqualTo(new Topic(new TopicId(1L), title));
        var topic2 = storage.addTopic(title);
        assertThat(topic2).isEqualTo(topic1);
    }

    @Test
    void getTopic() {
        var topicId = new TopicId(1L);
        assertThatThrownBy(() -> storage.getTopic(topicId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Topic was not found by id: TopicId[id=1]");
        var title = "Java";
        var topic1 = storage.addTopic(title);
        assertThat(topic1).isEqualTo(new Topic(new TopicId(1L), title));
        var topic2 = storage.getTopic(topicId);
        assertThat(topic2).isEqualTo(topic1);
    }

    @Test
    void saveTopic() {
        var topic1 = new Topic(new TopicId(5L), "Java");
        var topic2 = new Topic(new TopicId(10L), "Scala");
        storage.saveTopic(topic1);
        storage.saveTopic(topic2);
        assertThat(storage.getTopic(topic1.id())).isEqualTo(topic1);
        assertThat(storage.getTopic(topic2.id())).isEqualTo(topic2);
    }

    @Test
    void renameTopicNoCollision() {
        var topic = storage.addTopic("Java");
        var renamed = storage.renameTopic(topic.id(), "Kotlin");
        assertThat(renamed).isEqualTo(new Topic(topic.id(), "Kotlin"));
        assertThat(storage.getTopic(topic.id())).isEqualTo(renamed);
    }

    @Test
    void renameTopicNoOpSameTitle() {
        var topic = storage.addTopic("Java");
        var result = storage.renameTopic(topic.id(), "Java");
        assertThat(result).isEqualTo(topic);
    }

    @Test
    void renameTopicMergesOnCollision() {
        var topic1 = storage.addTopic("Java");
        var topic2 = storage.addTopic("Kotlin");
        storage.saveInteraction(newInteraction(1L, topic1));
        storage.saveInteraction(newInteraction(2L, topic1));
        var result = storage.renameTopic(topic1.id(), "Kotlin");
        assertThat(result).isEqualTo(topic2);
        assertThat(storage.readInteraction(new InteractionId(1L)).orElseThrow().topicId()).isEqualTo(topic2.id());
        assertThat(storage.readInteraction(new InteractionId(2L)).orElseThrow().topicId()).isEqualTo(topic2.id());
        var topic1Id = topic1.id();
        assertThatThrownBy(() -> storage.getTopic(topic1Id)).isInstanceOf(IllegalStateException.class);
        assertThat(storage.getTopics()).containsExactly(topic2);
    }

    @Test
    void renameTopicTrimsInput() {
        var topic = storage.addTopic("Java");
        var renamed = storage.renameTopic(topic.id(), "  Kotlin  ");
        assertThat(renamed.title()).isEqualTo("Kotlin");
    }

    private static Interaction newInteraction(long id, Topic topic) {
        return new Interaction(new InteractionId(id), null, topic.id(), null, null, null);
    }
}
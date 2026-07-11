package gptui.ui.model.storage;

import com.google.inject.Guice;
import gptui.RootModule;

import java.util.concurrent.atomic.AtomicInteger;

import static gptui.core.storagefilesystem.AnswerType.CLAUDE;
import static gptui.core.storagefilesystem.AnswerType.GCP;
import static gptui.core.storagefilesystem.AnswerType.GRAMMAR;
import static gptui.core.storagefilesystem.AnswerType.SHORT;

public class StorageUpdater {

    static void main() {
        var injector = Guice.createInjector(new RootModule());
        var storageModel = injector.getInstance(StorageModel.class);
        convertInteractions(storageModel);
    }

    private static void convertInteractions(StorageModel storageModel) {
        var counter = new AtomicInteger();
        var temperature = 70;
        storageModel.readAllInteractions().stream()
                .map(interaction -> {
                    counter.incrementAndGet();
                    return interaction
                            .withAnswer(GRAMMAR, answer -> answer.withTemperature(temperature))
                            .withAnswer(SHORT, answer -> answer.withTemperature(temperature))
                            .withAnswer(CLAUDE, answer -> answer.withTemperature(temperature))
                            .withAnswer(GCP, answer -> answer.withTemperature(temperature));
                })
                .forEach(storageModel::saveInteraction);
        System.out.println("Counter: " + counter.get());
    }
}

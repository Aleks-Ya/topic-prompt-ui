package gptui.ui.model.question.question;

import gptui.core.ai.ConversationTurn;
import gptui.core.storagefilesystem.AnswerState;
import gptui.core.storagefilesystem.AnswerType;
import gptui.core.storagefilesystem.Interaction;
import gptui.core.storagefilesystem.InteractionId;
import gptui.ui.model.storage.StorageModel;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static gptui.core.ai.ConversationTurn.Speaker.MODEL;
import static gptui.core.ai.ConversationTurn.Speaker.USER;

@Singleton
class FollowUpHistoryBuilder {
    private final StorageModel storage;

    @Inject
    FollowUpHistoryBuilder(StorageModel storage) {
        this.storage = storage;
    }

    List<ConversationTurn> buildHistory(InteractionId parentInteractionId, AnswerType answerType) {
        Deque<Interaction> ancestors = new ArrayDeque<>();
        var currentId = parentInteractionId;
        while (currentId != null) {
            var lookupId = currentId;
            var interaction = storage.readInteraction(lookupId).orElseThrow(() ->
                    new IllegalStateException("Ancestor interaction not found: " + lookupId));
            ancestors.addFirst(interaction);
            currentId = interaction.parentInteractionId();
        }

        var turns = new ArrayList<ConversationTurn>();
        for (var ancestor : ancestors) {
            var answer = ancestor.getAnswer(answerType).orElseThrow(() -> new IllegalStateException(
                    "Ancestor interaction has no answer for " + answerType + ": " + ancestor.id()));
            if (answer.answerState() != AnswerState.SUCCESS) {
                throw new IllegalStateException(
                        "Ancestor interaction has no successful answer for " + answerType + ": " + ancestor.id());
            }
            turns.add(new ConversationTurn(USER, answer.prompt()));
            turns.add(new ConversationTurn(MODEL, answer.answerMd()));
        }
        return turns;
    }
}

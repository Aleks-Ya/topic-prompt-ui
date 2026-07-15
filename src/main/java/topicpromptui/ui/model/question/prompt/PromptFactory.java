package topicpromptui.ui.model.question.prompt;

import topicpromptui.core.storagefilesystem.AnswerType;
import topicpromptui.core.storagefilesystem.InteractionType;

import java.util.Optional;

public interface PromptFactory {
    Optional<String> getPrompt(InteractionType interactionType, String topic, String question, AnswerType answerType);
}

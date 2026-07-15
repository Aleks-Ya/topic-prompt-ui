package gptui.ui.model.question.prompt;

import gptui.core.storagefilesystem.AnswerType;
import gptui.core.storagefilesystem.InteractionType;

import java.util.Optional;

public interface PromptFactory {
    Optional<String> getPrompt(InteractionType interactionType, String topic, String question, AnswerType answerType);
}

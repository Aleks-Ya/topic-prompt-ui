package gptui.model.question.prompt;

import gptui.model.storage.AnswerType;
import gptui.model.storage.InteractionType;

import java.util.Optional;

public interface PromptFactory {
    Optional<String> getPrompt(InteractionType interactionType, String theme, String question, AnswerType answerType);
}

package gptui.ui.model.question.prompt;

import gptui.ui.model.storage.AnswerType;
import gptui.ui.model.storage.InteractionType;

import java.util.Optional;

public interface PromptFactory {
    Optional<String> getPrompt(InteractionType interactionType, String theme, String question, AnswerType answerType);
}

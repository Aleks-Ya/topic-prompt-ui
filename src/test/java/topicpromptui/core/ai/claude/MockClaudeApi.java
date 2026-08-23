package topicpromptui.core.ai.claude;

import topicpromptui.core.ai.AiApi;
import topicpromptui.ui.model.question.BaseMockApi;
import jakarta.inject.Singleton;

/**
 * Bound in {@code TestRootModule} even though no {@code AnswerType} slot currently uses Claude: without
 * it, pointing a slot at {@link topicpromptui.core.domain.AiProvider#CLAUDE} would silently send every
 * UI test to the real Claude API. Stubbing helpers live on {@link BaseMockApi}; add named ones here
 * only once a slot uses Claude.
 */
@Singleton
public class MockClaudeApi extends BaseMockApi implements AiApi {
}

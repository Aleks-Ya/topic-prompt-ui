package topicpromptui.core.ai.xai;

import topicpromptui.core.ai.AiApi;
import topicpromptui.ui.model.question.BaseMockApi;
import jakarta.inject.Singleton;

/**
 * Bound in {@code TestRootModule} even though no {@code AnswerType} slot currently uses XAI: without
 * it, pointing a slot at {@link topicpromptui.core.domain.AiProvider#XAI} would silently send every
 * UI test to the real xAI API (confirmed - the pane rendered a live "Incorrect API key" error).
 * Stubbing helpers live on {@link BaseMockApi}; add named ones here only once a slot uses XAI.
 */
@Singleton
public class MockXaiApi extends BaseMockApi implements AiApi {
}

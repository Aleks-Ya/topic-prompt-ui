package topicpromptui.ui.answer;

import javafx.scene.web.WebView;
import org.junit.jupiter.api.Test;
import topicpromptui.BaseTopicPromptUiTest;
import topicpromptui.core.domain.Citation;
import topicpromptui.core.domain.Interaction;
import topicpromptui.ui.TestingData.I1;

import java.util.List;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static topicpromptui.core.domain.AnswerType.AI_1;

class AnswerCitationsUiTest extends BaseTopicPromptUiTest {
    private static final Citation CITATION = new Citation("https://nodejs.org/releases", "Node.js Releases");

    @Override
    public void init() {
        storage.saveTopic(I1.TOPIC);
        storage.saveInteraction(withCitationsOnAi1(I1.INTERACTION));
    }

    @Test
    void sourcesAreListedUnderTheAnswerOfTheCitingPaneOnly() {
        var ai1Html = webViewContent(ai1Answer().webView());
        assertThat(ai1Html).contains(I1.AI_1_HTML)
                .contains("Sources")
                .contains("href=\"https://nodejs.org/releases\"")
                .contains(">Node.js Releases</a>");
        assertThat(webViewContent(grammarAnswer().webView())).doesNotContain("Sources");
    }

    @Test
    void clickingASourceOpensItOutsideTheWebView() {
        // A scripted DOM click, not a TestFX coordinate click: it exercises the same injected
        // JavaScript listener and does not depend on where the footer lands inside the pane.
        executeSyncInFxThread(() -> ai1Answer().webView().getEngine()
                .executeScript("document.querySelector('.source-link').click()"));

        assertThat(fileModel.getOpenedUrls()).containsExactly(CITATION.url());
        // The click must not have navigated the pane away from the answer.
        assertThat(webViewContent(ai1Answer().webView())).contains(I1.AI_1_HTML);
    }

    @Test
    void copyingAnAnswerExcludesTheSourcesFooter() {
        clickOn(ai1Answer().copyButton());

        var copied = new String[1];
        executeSyncInFxThread(() -> copied[0] = clipboardModel.getTextFromClipboard());
        assertThat(copied[0]).contains(I1.AI_1_HTML)
                .doesNotContain("Sources")
                .doesNotContain(CITATION.url());
    }

    private String webViewContent(WebView webView) {
        var content = new String[1];
        executeSyncInFxThread(() -> content[0] =
                (String) webView.getEngine().executeScript("document.documentElement.outerHTML"));
        return content[0];
    }

    private static Interaction withCitationsOnAi1(Interaction interaction) {
        var answers = new TreeMap<>(interaction.answers());
        answers.put(AI_1, answers.get(AI_1).withCitations(List.of(CITATION)));
        return new Interaction(interaction.id(), interaction.type(), interaction.topicId(), interaction.question(),
                answers, interaction.parentInteractionId());
    }
}

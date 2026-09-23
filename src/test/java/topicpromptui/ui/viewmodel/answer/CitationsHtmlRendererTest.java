package topicpromptui.ui.viewmodel.answer;

import org.junit.jupiter.api.Test;
import topicpromptui.core.domain.Citation;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CitationsHtmlRendererTest {

    @Test
    void answersWithoutCitationsAreUnchanged() {
        assertThat(CitationsHtmlRenderer.withSources("<p>Answer</p>", null)).isEqualTo("<p>Answer</p>");
        assertThat(CitationsHtmlRenderer.withSources("<p>Answer</p>", List.of())).isEqualTo("<p>Answer</p>");
    }

    @Test
    void sourcesAreListedInOrderBelowTheAnswer() {
        var html = CitationsHtmlRenderer.withSources("<p>Answer</p>", List.of(
                new Citation("https://nodejs.org/releases", "Node.js Releases"),
                new Citation("https://blog.nodejs.org/v24", "blog.nodejs.org")));
        assertThat(html).startsWith("<p>Answer</p>")
                .contains("<hr>Sources")
                .contains("""
                        <li><a class="source-link" href="https://nodejs.org/releases">Node.js Releases</a></li>\
                        <li><a class="source-link" href="https://blog.nodejs.org/v24">blog.nodejs.org</a></li>""");
    }

    @Test
    void aCitationWithoutTitleShowsItsUrlAsTheLinkText() {
        var html = CitationsHtmlRenderer.withSources("", List.of(
                new Citation("https://nodejs.org/a", null),
                new Citation("https://nodejs.org/b", " ")));
        assertThat(html).contains(">https://nodejs.org/a</a>").contains(">https://nodejs.org/b</a>");
    }

    @Test
    void withoutSourcesUndoesWithSources() {
        var answer = "<p>Answer</p>";
        var html = CitationsHtmlRenderer.withSources(answer, List.of(
                new Citation("https://nodejs.org/releases", "Node.js Releases")));
        assertThat(CitationsHtmlRenderer.withoutSources(html)).isEqualTo(answer);
    }

    @Test
    void withoutSourcesFindsTheFooterInTheEnginesNormalizedMarkup() {
        var normalized = """
                <html><head></head><body><p>Answer</p>\
                <div style="color:#666" id="tpui-sources"><hr>Sources<ol>\
                <li><a class="source-link" href="https://nodejs.org/a">A</a></li></ol></div>\
                </body></html>""";
        assertThat(CitationsHtmlRenderer.withoutSources(normalized))
                .isEqualTo("<html><head></head><body><p>Answer</p></body></html>");
    }

    @Test
    void withoutSourcesLeavesAnAnswerWithNoFooterAlone() {
        assertThat(CitationsHtmlRenderer.withoutSources("<p>Answer</p>")).isEqualTo("<p>Answer</p>");
        assertThat(CitationsHtmlRenderer.withoutSources(null)).isEmpty();
    }

    @Test
    void titlesAndUrlsAreHtmlEscaped() {
        var html = CitationsHtmlRenderer.withSources("", List.of(
                new Citation("https://x.org/?a=1&b=2", "<b>Tom</b> & \"Jerry\"")));
        assertThat(html).contains("href=\"https://x.org/?a=1&amp;b=2\"")
                .contains(">&lt;b&gt;Tom&lt;/b&gt; &amp; &quot;Jerry&quot;</a>");
    }
}

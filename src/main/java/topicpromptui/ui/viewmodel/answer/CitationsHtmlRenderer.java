package topicpromptui.ui.viewmodel.answer;

import topicpromptui.core.domain.Citation;

import java.util.List;

/**
 * Appends the cited web sources below an answer's HTML at display time. The result is never
 * persisted: {@code Answer.answerHtml} stays exactly what the model produced, so stored answers
 * that predate this rendering show their sources as soon as they are displayed again.
 */
final class CitationsHtmlRenderer {
    private static final String FOOTER_STYLE = "margin-top:1em;font-size:0.85em;color:#666;";

    private CitationsHtmlRenderer() {
    }

    static String withSources(String answerHtml, List<Citation> citations) {
        var html = answerHtml != null ? answerHtml : "";
        if (citations == null || citations.isEmpty()) {
            return html;
        }
        var sb = new StringBuilder(html)
                .append("<div style=\"").append(FOOTER_STYLE).append("\"><hr>Sources<ol>");
        for (var citation : citations) {
            // Titles are provider-dependent: a real page title from Claude/OpenAI, a bare domain
            // from Gemini, and sometimes absent altogether - then the URL alone is the link text.
            var title = citation.title() == null || citation.title().isBlank()
                    ? citation.url() : citation.title();
            sb.append("<li><a class=\"source-link\" href=\"").append(escape(citation.url()))
                    .append("\">").append(escape(title)).append("</a></li>");
        }
        return sb.append("</ol></div>").toString();
    }

    // Provider text lands in both an attribute and element content, and titles really do contain
    // ampersands and quotes.
    private static String escape(String text) {
        return text == null ? "" : text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}

package topicpromptui.ui.viewmodel.answer;

import topicpromptui.core.domain.Citation;

import java.util.List;

/**
 * Appends the cited web sources below an answer's HTML at display time. The result is never
 * persisted: {@code Answer.answerHtml} stays exactly what the model produced, so stored answers
 * that predate this rendering show their sources as soon as they are displayed again.
 */
final class CitationsHtmlRenderer {
    private static final String FOOTER_ID = "tpui-sources";
    private static final String FOOTER_STYLE = "margin-top:1em;font-size:0.85em;color:#666;";
    private static final String DIV_CLOSE = "</div>";

    private CitationsHtmlRenderer() {
    }

    static String withSources(String answerHtml, List<Citation> citations) {
        var html = answerHtml != null ? answerHtml : "";
        if (citations == null || citations.isEmpty()) {
            return html;
        }
        var sb = new StringBuilder(html)
                .append("<div id=\"").append(FOOTER_ID).append("\" style=\"").append(FOOTER_STYLE)
                .append("\"><hr>Sources<ol>");
        for (var citation : citations) {
            // Titles are provider-dependent: a real page title from Claude/OpenAI, a bare domain
            // from Gemini, and sometimes absent altogether - then the URL alone is the link text.
            var title = citation.title() == null || citation.title().isBlank()
                    ? citation.url() : citation.title();
            sb.append("<li><a class=\"source-link\" href=\"").append(escape(citation.url()))
                    .append("\">").append(escape(title)).append("</a></li>");
        }
        return sb.append("</ol>").append(DIV_CLOSE).toString();
    }

    /**
     * Inverse of {@link #withSources}, for the Copy button. The input is the WebView's normalized
     * {@code outerHTML} rather than the string {@code withSources} produced (the view reads it back
     * from the engine), so the footer is located by its marker id instead of by exact markup; it
     * contains no nested {@code <div>}, hence the first closing tag after the marker ends it.
     */
    static String withoutSources(String html) {
        if (html == null) {
            return "";
        }
        var marker = html.indexOf("id=\"" + FOOTER_ID + "\"");
        if (marker < 0) {
            return html;
        }
        var start = html.lastIndexOf("<div", marker);
        var end = html.indexOf(DIV_CLOSE, marker);
        if (start < 0 || end < 0) {
            return html;
        }
        return html.substring(0, start) + html.substring(end + DIV_CLOSE.length());
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

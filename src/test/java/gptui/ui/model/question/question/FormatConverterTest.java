package gptui.ui.model.question.question;

import gptui.BaseTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class FormatConverterTest extends BaseTest {
    private final FormatConverter formatConverter = injector.getInstance(FormatConverter.class);

    static Stream<Arguments> markdownToHtmlCases() {
        return Stream.of(
                Arguments.of("markdownToHtml", """
                        # Header 1
                        Bold text: *bold*
                        """, """
                        <h1>Header 1</h1>
                        <p>Bold text: <em>bold</em></p>
                        """),
                Arguments.of("tables", """
                        Table 1

                        | AA | BB |
                        |----|----|
                        | 11 | 22 |""", """
                        <p>Table 1</p>
                        <table>
                        <thead>
                        <tr><th>AA</th><th>BB</th></tr>
                        </thead>
                        <tbody>
                        <tr><td>11</td><td>22</td></tr>
                        </tbody>
                        </table>
                        """),
                Arguments.of("expandNestedMarkDownBlocks", """
                        Data:
                        ```markdown
                        1. AAA
                        2. BBB
                        ```

                        Should skip:
                        ```plaintext
                        Hi, FlexMark
                        ```

                        New Data:
                        ```markdown
                        # Head 1
                        *Important*
                        ```
                        """, """
                        <p>Data:</p>
                        <ol>
                        <li>AAA</li>
                        <li>BBB</li>
                        </ol>
                        <p>Should skip:</p>
                        <pre><code class="language-plaintext">Hi, FlexMark
                        </code></pre>
                        <p>New Data:</p>
                        <h1>Head 1</h1>
                        <p><em>Important</em></p>
                        """)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("markdownToHtmlCases")
    void markdownToHtml(String caseName, String md, String expectedHtml) {
        var html = formatConverter.markdownToHtml(md);
        assertThat(html).isEqualTo(expectedHtml);
    }
}

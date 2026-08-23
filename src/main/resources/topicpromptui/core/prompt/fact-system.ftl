<#--noinspection HtmlUnknownTag-->
Check whether the given statement is factually correct in the context of the topic `${topic}`.

<guidelines>
    <guideline>Format your answer into Markdown</guideline>
    <guideline>
        Do not narrate or announce tool or documentation lookups.
        Output only the final answer with no preamble.
    </guideline>
    <guideline>
        Start with the verdict as a single bolded word or phrase: `**Correct**`, `**Incorrect**` or
        `**Partly correct**`.
    </guideline>
    <guideline>
        If the statement is correct, answer with the verdict alone and add nothing after it.
        Otherwise, follow the verdict with at most three sentences stating what is wrong, then end with the
        corrected statement on its own last line, prefixed with `**Correct version:**`.
        Keep the corrected statement as close to my wording as the facts allow: fix only what is wrong and never
        rephrase or expand the rest, so that I can send the line back to you as a new statement.
        <example>
            <example-topic>HTTP</example-topic>
            <example-statement>HTTP/2 removes head-of-line blocking.</example-statement>
            <your-answer>
                **Incorrect**

                HTTP/2 removes head-of-line blocking only at the HTTP layer, where independent streams share one
                connection.
                A lost TCP segment still stalls every stream until it is retransmitted.

                **Correct version:** HTTP/2 removes head-of-line blocking at the HTTP layer, but TCP-level head-of-line
                blocking remains.
            </your-answer>
        </example>
    </guideline>
    <guideline>
        Do not exceed roughly 100 words.
        Omit background, history, caveats and edge cases unless they are the reason the statement is wrong.
        I send a follow-up question when I want more.
    </guideline>
    <guideline>
        Do not restate the verdict at the end.
        The `**Correct version:**` line is the only place my statement may reappear.
    </guideline>
    <guideline>
        Prefer plain prose. Do not use headings.
        Use a bulleted list only when the statement bundles several distinct claims that need separate verdicts;
        even then, close with a single `**Correct version:**` line covering the whole statement.
    </guideline>
</guidelines>

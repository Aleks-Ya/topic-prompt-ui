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
        Otherwise, follow the verdict with at most three sentences stating what is wrong and what the correct fact is.
    </guideline>
    <guideline>
        Do not exceed roughly 80 words.
        Omit background, history, caveats and edge cases unless they are the reason the statement is wrong.
        I send a follow-up question when I want more.
    </guideline>
    <guideline>Do not repeat the statement back to me and do not restate the verdict at the end</guideline>
    <guideline>
        Prefer plain prose. Do not use headings.
        Use a bulleted list only when the statement bundles several distinct claims that need separate verdicts.
    </guideline>
</guidelines>

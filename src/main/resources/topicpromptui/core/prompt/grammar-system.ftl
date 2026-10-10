<#--noinspection HtmlUnknownTag-->
I will give you a sentence or phrase in the context of the topic `${topic}`.
Check if the sentence or phrase has grammatical mistakes.

<guidelines>
    <guideline>
        <#-- LLMs often consider my questions that start with "How to" as a mistake -->
        It is not a mistake if the sentence or phrase starts with `How to`.
    </guideline>
    <guideline>
        It is not a mistake if a term starts with a capital letter mid-sentence, since this can be intentional to denote
        a proper noun or a specific term.
        E.g. `Resume is a Slash Command in Claude Code that reopens a previous session.`
    </guideline>
    <guideline>
        It is not a mistake if the sentence or phrase starts with a term without an article (`a`, `an`, `the`),
        since the article is optional when the sentence defines that term. Never add an article to the beginning
        of the sentence or phrase.
        E.g. `Certificate chain is a sequence of certificates connecting a subject to a trusted root certificate.`
    </guideline>
    <guideline>
        If the sentence or phrase is correct, answer exactly `Correct` and nothing else.
        Never repeat my sentence back to me unchanged: if you would not change a single character, answer `Correct`.
        Otherwise, answer with the corrected sentence only: no preamble, no explanation, no list of changes.
        <example>
            <user-sentence>What is the difference between a thread and a process?</user-sentence>
            <your-answer>Correct</your-answer>
        </example>
        <example>
            <user-sentence>How to configure a connection pool in Spring Boot?</user-sentence>
            <your-answer>Correct</your-answer>
        </example>
    </guideline>
    <guideline>
        Correct grammar only: never swap a term for another one, never rephrase, and never change the meaning of my
        sentence.
    </guideline>
</guidelines>

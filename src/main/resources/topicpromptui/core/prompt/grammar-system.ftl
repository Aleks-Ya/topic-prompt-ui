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
        If the sentence or phrase is correct, answer exactly `Correct` and nothing else.
        Otherwise, answer with the corrected sentence only: no preamble, no explanation, no list of changes.
        <example>
            <user-sentence>What is the difference between a thread and a process?</user-sentence>
            <your-answer>Correct</your-answer>
        </example>
    </guideline>
    <guideline>
        Correct grammar only: never swap a term for another one, never rephrase, and never change the meaning of my
        sentence.
    </guideline>
    <guideline>
        Mark every word you changed or added by wrapping it in Markdown bold: **word**.
        <example>
            <user-sentence>How to configure a EC2 instance?</user-sentence>
            <your-answer>How to configure **an** EC2 instance?</your-answer>
        </example>
    </guideline>
    <guideline>
        Bold the smallest possible span: only the words that actually differ from my sentence.
        Never bold a whole clause or sentence, and never bold unchanged words next to a changed one, except when
        marking a removal.
        <example>
            <user-sentence>He go to the office yesterday and speak with the team.</user-sentence>
            <your-answer>He **went** to the office yesterday and **spoke** with the team.</your-answer>
        </example>
    </guideline>
    <guideline>
        If a correction only removes a word, bold the word immediately before the removed word and the word immediately
        after it, so that I can see where the removal happened. Bold only these two neighbouring words and nothing
        wider.
        If the removed word was at the very start or the very end of the sentence, bold only the single neighbouring
        word that exists.
        <example>
            <user-sentence>I discussed about the problem with the team.</user-sentence>
            <your-answer>I **discussed the** problem with the team.</your-answer>
        </example>
    </guideline>
    <guideline>
        Before you answer, compare your corrected sentence with mine word by word:
        every word inside ** ** must either really differ from mine or be a neighbour of a removed word,
        and every differing word must be inside ** **.
    </guideline>
</guidelines>

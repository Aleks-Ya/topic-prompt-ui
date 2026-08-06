I will give you a sentence or phrase in the context of the topic `${topic}`.
Check if the sentence or phrase has grammatical mistakes.
It is not a mistake if the sentence or phrase starts with `How to`.<#-- LLMs often concider my questions that start with "How to" as a mistake -->
It is not a mistake if a term starts with a capital letter mid-sentence, since this can be intentional to denote a proper noun or a specific term (e.g. `Resume is a Slash Command in Claude Code that reopens a previous session.`).

Answer format:
— If the sentence or phrase is correct, answer exactly `Correct` and nothing else.
— Otherwise answer with the corrected sentence only: no preamble, no explanation, no list of changes.
— Mark every word you changed or added by wrapping it in Markdown bold: **word**.
— Bold the smallest possible span: only the words that actually differ from my sentence. Never bold a whole clause or sentence, and never bold unchanged words next to a changed one, except to mark a removal as described in the next rule.
— If a correction only removes a word, bold the word immediately before the removed word and the word immediately after it, so that I can see where the removal happened. Bold only these two neighbouring words and nothing wider. If the removed word was at the very start or the very end of the sentence, bold only the single neighbouring word that exists.
— Before you answer, compare your corrected sentence with mine word by word: every word inside ** ** must either really differ from mine or be a neighbour of a removed word, and every differing word must be inside ** **.

Examples:
My sentence: How to configure a EC2 instance?
Your answer: How to configure **an** EC2 instance?

My sentence: He go to the office yesterday and speak with the team.
Your answer: He **went** to the office yesterday and **spoke** with the team.

My sentence: I discussed about the problem with the team.
Your answer: I **discussed the** problem with the team.

My sentence: What is the difference between a thread and a process?
Your answer: Correct

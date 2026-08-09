package topicpromptui.ui.model.question;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Chunk;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Singleton
class GrammarDiffMarker {
    private static final String CORRECT_SENTINEL = "Correct";
    private static final Pattern WORD = Pattern.compile("\\S+");

    String markChanges(String question, String correctedMd) {
        if (question == null || question.isBlank() || correctedMd == null || correctedMd.isBlank()) {
            return correctedMd;
        }
        var corrected = correctedMd.replace("**", "");
        if (CORRECT_SENTINEL.equals(corrected.trim())) {
            return corrected;
        }
        var correctedWords = split(corrected);
        var boldIndexes = boldIndexes(split(question), correctedWords);
        return render(corrected, correctedWords, boldIndexes);
    }

    private Set<Integer> boldIndexes(List<Word> questionWords, List<Word> correctedWords) {
        var deltas = DiffUtils.diff(texts(questionWords), texts(correctedWords)).getDeltas();
        var boldIndexes = new HashSet<Integer>();
        for (var delta : deltas) {
            Chunk<String> target = delta.getTarget();
            switch (delta.getType()) {
                case INSERT, CHANGE -> {
                    for (var i = 0; i < target.size(); i++) {
                        boldIndexes.add(target.getPosition() + i);
                    }
                }
                case DELETE -> {
                    addIfPresent(boldIndexes, target.getPosition() - 1, correctedWords.size());
                    addIfPresent(boldIndexes, target.getPosition(), correctedWords.size());
                }
                case EQUAL -> { /* not produced by DiffUtils.diff */ }
            }
        }
        return boldIndexes;
    }

    private String render(String corrected, List<Word> words, Set<Integer> boldIndexes) {
        if (boldIndexes.isEmpty()) {
            return corrected;
        }
        var sb = new StringBuilder();
        var cursor = 0;
        var i = 0;
        while (i < words.size()) {
            if (!boldIndexes.contains(i)) {
                i++;
                continue;
            }
            var last = i;
            while (last + 1 < words.size() && boldIndexes.contains(last + 1) && sameLine(corrected, words, last)) {
                last++;
            }
            sb.append(corrected, cursor, words.get(i).start())
                    .append("**").append(corrected, words.get(i).start(), words.get(last).end()).append("**");
            cursor = words.get(last).end();
            i = last + 1;
        }
        return sb.append(corrected, cursor, corrected.length()).toString();
    }

    private boolean sameLine(String corrected, List<Word> words, int index) {
        return corrected.lastIndexOf('\n', words.get(index + 1).start()) < words.get(index).end();
    }

    private void addIfPresent(Set<Integer> boldIndexes, int index, int size) {
        if (index >= 0 && index < size) {
            boldIndexes.add(index);
        }
    }

    private List<Word> split(String text) {
        var words = new ArrayList<Word>();
        var matcher = WORD.matcher(text);
        while (matcher.find()) {
            words.add(new Word(matcher.group(), matcher.start(), matcher.end()));
        }
        return words;
    }

    private List<String> texts(List<Word> words) {
        return words.stream().map(Word::text).toList();
    }

    private record Word(String text, int start, int end) {
    }
}

package org.grahamkirby.race_timing.common;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("StringTemplateMigration")
public class Normalisation {

    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final double NANOSECONDS_PER_SECOND = 1000000000.0;

    private static final Set<Character> WORD_SEPARATORS = Set.of(' ', '-', '\'', '’');
    private static final Map<String, String> REMOVE_DOUBLE_SPACES = Map.of("  ", " ");

    private final Race race;

    public Normalisation(final Race race) {
        this.race = race;
    }

    static String getFirstName(final String name) {
        return name.split(" ")[0];
    }

    static String getLastName(final String name) {

        return Arrays.stream(name.split(" ")).toList().getLast();
    }

    public String cleanRunnerName(final String name) {

        // Remove extra whitespace.
        final String step1 = replaceAllMapEntries(name, REMOVE_DOUBLE_SPACES);
        final String step2 = step1.strip();

        return toTitleCase(step2);
    }

    public String cleanClubOrTeamName(final String name) {

        // Remove extra whitespace.
        final String step1 = replaceAllMapEntries(name, REMOVE_DOUBLE_SPACES);
        final String step2 = step1.strip();

        // Check normalisation list (case insensitive).
        if (race.normalised_club_names.containsKey(step2)) return race.normalised_club_names.get(step2);

        return toTitleCase(step2);
    }

    public String htmlEncode(final String s) {

        return replaceAllMapEntries(s, race.normalised_html_entities);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private String toTitleCase(final String input) {

        final String s = lookupInStopWords(input);
        if (s != null) return s;

        final StringBuilder result = new StringBuilder();

        while (result.length() < input.length())
            processNextWord(input, result);

        return result.toString();
    }

    private void processNextWord(final String input, final StringBuilder builder) {

        char separator = 0;
        int i;
        for (i = builder.length(); i < input.length(); i++) {
            final char c = input.charAt(i);
            if (WORD_SEPARATORS.contains(c)) {
                separator = c;
                break;
            }
        }

        final String next_word = input.substring(builder.length(), i);

        builder.append(toTitleCaseWord(next_word));
        if (separator > 0) builder.append(separator);
    }

    private String toTitleCaseWord(final String word) {

        final String s = lookupInStopWords(word);
        if (s != null) return s;

        if (word.isEmpty() || isTitleCase(word)) return word;

        race.non_title_case_words.add(word);
        return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
    }

    private String lookupInStopWords(final String word) {

        // Try case sensitive match first.
        if (race.capitalisation_stop_words.contains(word)) return word;

        // Try case insensitive match.
        return race.capitalisation_stop_words.stream().
            filter(w -> w.equalsIgnoreCase(word)).
            findFirst().
            orElse(null);
    }

    @SuppressWarnings("TypeMayBeWeakened")
    private static boolean isTitleCase(final String input) {

        return !Character.isLowerCase(input.charAt(0)) &&
            input.chars().boxed().skip(1).noneMatch(Character::isUpperCase);
    }

    private static String replaceAllMapEntries(final String s, final Map<String, String> normalisation_map) {

        String result = s;

        for (final Map.Entry<String, String> entry : normalisation_map.entrySet()) {

            final String value = entry.getValue();
            result = result.replaceAll(STR."(?i)\{entry.getKey()}", value);
        }
        return result;
    }

    public static Duration parseTime(final String element) {

        try {
            return parseTime(element, ":");
        } catch (final RuntimeException _) {
            return parseTime(element, "\\.");
        }
    }

    private static Duration parseTime(String element, final String separator) {

        element = element.strip();
        if (element.startsWith(separator)) element = STR."0\{element}";
        if (element.endsWith(separator)) element = STR."\{element}0";

        try {
            final String[] parts = element.split(separator);

            return Duration.parse(STR."PT\{hours(parts)}\{minutes(parts)}\{seconds(parts)}");

        } catch (final RuntimeException _) {
            throw new RuntimeException(STR."illegal time: \{element}");
        }
    }

    public static String format(final Duration duration) {

        final long seconds = duration.getSeconds();
        final int n = duration.getNano();

        String result = String.format("0%d:%02d:%02d", seconds / SECONDS_PER_HOUR, (seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE, (seconds % SECONDS_PER_MINUTE));
        if (n > 0) {
            final double fractional_seconds = n / NANOSECONDS_PER_SECOND;
            result += String.format("%1$,.3f", fractional_seconds).substring(1);
            while (result.endsWith("0")) result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private static String hours(final String[] parts) {
        return parts.length > 2 ? STR."\{parts[0]}H" : "";
    }

    private static String minutes(final String[] parts) {
        return (parts.length > 2 ? parts[1] : parts[0]) + "M";
    }

    private static String seconds(final String[] parts) {
        return (parts.length > 2 ? parts[2] : parts[1]) + "S";
    }
}

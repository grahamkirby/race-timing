/*
 * Copyright 2025 Graham Kirby:
 * <https://github.com/grahamkirby/race-timing>
 *
 * This file is part of the module race-timing.
 *
 * race-timing is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * race-timing is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with race-timing. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.common;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class Normalisation {

    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final double NANOSECONDS_PER_SECOND = 1_000_000_000.0;

    private static final Set<Character> WORD_SEPARATORS = Set.of(' ', '-', '\'', '’');
    private static final Map<String, String> REMOVE_DOUBLE_SPACES = Map.of("  ", " ");

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private final Race race;

    public Normalisation(final Race race) {
        this.race = race;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Cleans name by removing extra whitespace and converting to title case, unless present
     * in stop list file. */
    public String cleanRunnerName(final String name) {

        // Remove extra whitespace.
        final String step1 = replaceAllMapEntries(name, REMOVE_DOUBLE_SPACES);
        final String step2 = step1.strip();

        // Convert to title case, unless present in stop list.
        return toTitleCase(step2);
    }

    /** Cleans name by removing extra whitespace and normalising if present in normalisation file,
     * otherwise converting to title case, unless present in stop list file. */
    public String cleanClubOrTeamName(final String name) {

        // Remove extra whitespace.
        final String step1 = replaceAllMapEntries(name, REMOVE_DOUBLE_SPACES);
        final String step2 = step1.strip();

        // Check normalisation list (which is case insensitive for keys).
        if (race.normalised_club_names.containsKey(step2)) return race.normalised_club_names.get(step2);

        // Convert to title case, unless present in stop list.
        return toTitleCase(step2);
    }

    /** Replaces any accented characters with HTML entity codes. */
    public String htmlEncode(final String s) {
        return replaceAllMapEntries(s, race.normalised_html_entities);
    }

    /** Gets the first element of the array resulting from splitting the given name on the space character. */
    static String getFirstName(final String name) {
        return name.split(" ")[0];
    }

    /** Gets the last element of the array resulting from splitting the given name on the space character. */
    static String getLastName(final String name) {
        return Arrays.stream(name.split(" ")).toList().getLast();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Converts the given string to title case, ignoring any words present in the stop word file. */
    private String toTitleCase(final String input) {

        final String s = lookupInStopWords(input);
        if (s != null) return s;

        final StringBuilder result = new StringBuilder();

        while (result.length() < input.length())
            addNextWord(input, result);

        return result.toString();
    }

    /** Checks whether the given word is present in the stop word file, first with exact match
     * and then case insensitive. Returns the matching word if found, otherwise null. */
    private String lookupInStopWords(final String word) {

        // Try case sensitive match first.
        if (race.capitalisation_stop_words.contains(word)) return word;

        // Try case insensitive match.
        return race.capitalisation_stop_words.stream().
            filter(w -> w.equalsIgnoreCase(word)).
            findFirst().
            orElse(null);
    }

    /** Finds the next word in the given input not already added to the builder, and adds it
     * after converting to title case. */
    private void addNextWord(final String input, final StringBuilder builder) {

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

    /** Converts the given word to title case, unless present in stop word file. */
    private String toTitleCaseWord(final String word) {

        final String s = lookupInStopWords(word);
        if (s != null) return s;

        if (word.isEmpty() || isTitleCase(word)) return word;

        race.non_title_case_words.add(word);
        return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
    }

    /** Tests whether given word has title case. */
    @SuppressWarnings("TypeMayBeWeakened")
    private static boolean isTitleCase(final String word) {

        return !Character.isLowerCase(word.charAt(0)) &&
            word.chars().boxed().skip(1).noneMatch(Character::isUpperCase);
    }

    /** For each map entry, searches for instances of the key in the given string (case insensitive)
     * and replaces each one with the corresponding value. */
    private static String replaceAllMapEntries(final String s, final Map<String, String> normalisation_map) {

        String result = s;

        for (final Map.Entry<String, String> entry : normalisation_map.entrySet())
            // "(?i)" specifies case insensitive map lookup.
            result = result.replaceAll(STR."(?i)\{entry.getKey()}", entry.getValue());

        return result;
    }

    /** Parses the given time string, trying both colon and full stop as separators. */
    public static Duration parseTime(final String time) {

        try {
            return parseTime(time, ":");
        } catch (final RuntimeException _) {
            return parseTime(time, "\\.");
        }
    }

    /** Parses the given time in format hours/minutes/seconds or minutes/seconds, using the given separator. */
    private static Duration parseTime(String time, final String separator) {

        time = time.strip();

        // Deal with missing hours or seconds component.
        if (time.startsWith(separator)) time = STR."0\{time}";
        if (time.endsWith(separator)) time = STR."\{time}0";

        try {
            final String[] parts = time.split(separator);

            // Construct ISO-8601 duration format.
            return Duration.parse(STR."PT\{hours(parts)}\{minutes(parts)}\{seconds(parts)}");

        } catch (final RuntimeException _) {
            throw new RuntimeException(STR."illegal time: \{time}");
        }
    }

    /** Formats the given duration into a string in HH:MM:SS.SSS format, omitting fractional trailing zeros. */
    public static String format(final Duration duration) {

        final long total_seconds = duration.getSeconds();

        final long hours = total_seconds / SECONDS_PER_HOUR;
        final long minutes = (total_seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE;
        final long seconds = total_seconds % SECONDS_PER_MINUTE;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds) + formatFractionalPart(duration.getNano());
    }

    private static String formatFractionalPart(final int fractional_seconds_as_nanoseconds) {

        if (fractional_seconds_as_nanoseconds == 0) return "";

        final double fractional_seconds = fractional_seconds_as_nanoseconds / NANOSECONDS_PER_SECOND;

        // Round to 3 decimal places.
        final String formatted_fractional_seconds = String.format("%1$,.3f", fractional_seconds);

        // Omit the zero preceding the decimal point, and trailing zeros.
        return formatted_fractional_seconds.replaceAll("^0|0+$", "");
    }

    private static String hours(final String[] parts) {
        return parts.length > 2 ? STR."\{parts[0]}H" : "";
    }

    private static String minutes(final String[] parts) {
        return STR."\{parts.length > 2 ? parts[1] : parts[0]}M";
    }

    private static String seconds(final String[] parts) {
        return STR."\{parts.length > 2 ? parts[2] : parts[1]}S";
    }
}

/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (graham.kirby@st-andrews.ac.uk)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing_experimental.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Race.COMMENT_SYMBOL;
import static org.grahamkirby.race_timing.common.output.RaceOutput.DNF_STRING;
import static org.grahamkirby.race_timing_experimental.common.Config.*;

/** Support for normalisation of runner and club names, and entry categories, also standardised
 * formatting for times and HTML entities. */
public class Normalisation {

    public static final String SUFFIX_PDF = ".pdf";

    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final double NANOSECONDS_PER_SECOND = 1_000_000_000.0;


    /** Characters treated as word separators when converting string to title case. */
    private static final Set<Character> WORD_SEPARATORS = Set.of(' ', '-', '\'', '’');

    /** Used when replacing double spaces with single space. */
    private static final Map<String, String> DOUBLE_SPACE_REMOVAL_MAP = Map.of("  ", " ");

    /** Strings that should not be converted to title case. */
    private Set<String> capitalisation_stop_words;

    /** Map for entry category normalisation. */
    private Map<String, String> category_map;

    /** Mappings for non-standard entry column formats. */
    private List<String> entry_column_mappings;

    /** Records words within runner, club and team names that are not already in title case in the entry file. */
    private Set<String> non_title_case_words;

    /** Map from club name variants to normalised names. */
    private Map<String, String> normalised_club_names;

    /** Map from accented strings to corresponding entities. */
    private Map<String, String> normalised_html_entities;

    /**
     * Map from entry gender to eligible prize genders.
     * Value is read from configuration file using key KEY_GENDER_ELIGIBILITY_MAP_PATH.
     */
    public Map<String, List<String>> gender_eligibility_map;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private final Race race;

    public Normalisation(final Race race) {

        this.race = race;
        configure();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public String getNonTitleCaseWords() {

        return non_title_case_words.stream().
            sorted().
            collect(Collectors.joining(", "));
    }

    /** Maps the given category to normalised form if any. */
    public String normaliseCategoryShortName(final String category_short_name) {

        return category_map.getOrDefault(category_short_name, category_short_name);
    }

    /** Maps race entry elements as defined by the previously configured mapping. */
    public List<String> mapRaceEntryElements(final List<String> elements) {

        // Expected format of mappings: "1,3-2,4,5",
        // meaning elements 2 and 3 should be swapped and concatenated with a space to give compound element.

        return entry_column_mappings.stream().
            map(s -> getMappedElement(elements, s)).
            toList();
    }

    /** Cleans name by removing extra whitespace and converting to title case, unless present
     *  in stop list file. */
    public String cleanRunnerName(final String name) {

        // Remove extra whitespace.
        final String step1 = replaceAllMapEntries(name, DOUBLE_SPACE_REMOVAL_MAP);
        final String step2 = step1.strip();

        // Convert to title case, unless present in stop list.
        return toTitleCase(step2);
    }

    /** Cleans name by removing extra whitespace and normalising if present in normalisation file,
     *  otherwise converting to title case, unless present in stop list file. */
    public String cleanClubOrTeamName(final String name) {

        // Remove extra whitespace.
        final String step1 = replaceAllMapEntries(name, DOUBLE_SPACE_REMOVAL_MAP);
        final String step2 = step1.strip();

        // Check normalisation list (which is case insensitive for keys).
        if (normalised_club_names.containsKey(step2)) return normalised_club_names.get(step2);

        // Convert to title case, unless present in stop list.
        return toTitleCase(step2);
    }

    /** Replaces any accented characters with HTML entity codes. */
    public String htmlEncode(final String s) {
        return replaceAllMapEntries(s, normalised_html_entities);
    }

    /** Parses the given time string, trying both colon and full stop as separators. */
    public static Duration parseTime(final String time) {

        try {
            return parseTime(time, ":");
        } catch (final RuntimeException _) {
            return parseTime(time, "\\.");
        }
    }

    /** Formats the given duration into a string in HH:MM:SS.SSS format, omitting fractional trailing zeros. */
    public static String format(final Duration duration) {

        if (duration == null) return DNF_STRING;
        return formatWholePart(duration) + formatFractionalPart(duration);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void configure() {

        try {
            entry_column_mappings = loadEntryColumnMapping();
            category_map = loadCategoryMap();
            gender_eligibility_map = loadGenderEligibilityMap();
            normalised_club_names = loadNormalisationMap(KEY_NORMALISED_CLUB_NAMES_PATH, false);
            normalised_html_entities = loadNormalisationMap(KEY_NORMALISED_HTML_ENTITIES_PATH, true);

            Path capitalisation_stop_words_path = (Path) race.getConfig().get(KEY_CAPITALISATION_STOP_WORDS_PATH);
            capitalisation_stop_words = new HashSet<>(Files.readAllLines(capitalisation_stop_words_path));

            non_title_case_words = new HashSet<>();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, List<String>> loadGenderEligibilityMap() throws IOException {

        final Map<String, List<String>> map = new HashMap<>();

        final Path gender_eligibility_map_path = (Path) race.getConfig().get(KEY_GENDER_ELIGIBILITY_MAP_PATH);

        Files.readAllLines(race.getFullPath(gender_eligibility_map_path)).stream().
            filter(line -> !line.startsWith(COMMENT_SYMBOL)).
            forEachOrdered(line -> {
                final String[] elements = line.split(",");
                map.putIfAbsent(elements[0], new ArrayList<>());
                map.get(elements[0]).add(elements[1]);
            });

        return map;
    }

    private List<String> loadEntryColumnMapping() {

        // Columns can be re-ordered by permuting the column numbers, or combined into a single column with an intervening
        // space character, by grouping column numbers with a dash.
        // E.g. 1,3-2,4,5 would combine the second and third columns, reversing the order and concatenating with a space character.

        // TODO update default column map to deal with relay races.
        final String entry_column_map_string = (String) race.getConfig().get(KEY_ENTRY_COLUMN_MAP);

        return Arrays.asList(entry_column_map_string.split(","));
    }

    private Map<String, String> loadCategoryMap() throws IOException {

        final Map<String, String> map = new HashMap<>();

        final Path category_map_path = (Path) race.getConfig().get(KEY_CATEGORY_MAP_PATH);
        if (category_map_path != null) {

            Files.readAllLines(category_map_path).stream().
                filter(line -> !line.isEmpty()).
                filter(line -> !line.startsWith(COMMENT_SYMBOL)).
                forEachOrdered(line -> {
                    final String[] parts = line.split(",");
                    map.put(parts[0], parts[1]);
                });
        }

        return map;
    }

    private Map<String, String> loadNormalisationMap(final String path_key, final boolean key_case_sensitive) throws IOException {

        final Map<String, String> map = key_case_sensitive ? new HashMap<>() : new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        final Path path = (Path) race.getConfig().get(path_key);

        Files.readAllLines(path).forEach(line -> {

            final String[] parts = line.split(",");
            map.put(parts[0], parts[1]);
        });

        return map;
    }

    private static String getMappedElement(final List<String> elements, final String element_combination_map) {

        // If 'element_combination_map' contains "2" then the result is the second value in 'elements'.

        // If it contains "3-5-4" then the result is formed from the third, fifth and fourth values
        // in 'elements' concatenated with spaces.

        return Arrays.stream(element_combination_map.split("-")).
            map(column_number_as_string -> elements.get(Integer.parseInt(column_number_as_string) - 1)).
            collect(Collectors.joining(" "));
    }

    /** Gets the first element of the array resulting from splitting the given name on the space character. */
    public static String getFirstName(final String name) {
        return name.split(" ")[0];
    }

    /** Gets the last element of the array resulting from splitting the given name on the space character. */
    public static String getLastName(final String name) {
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
     *  and then case insensitive. Returns the matching word if found, otherwise null. */
    private String lookupInStopWords(final String word) {

        // Try case sensitive match first.
        if (capitalisation_stop_words.contains(word)) return word;

        // Try case insensitive match.
        return capitalisation_stop_words.stream().
            filter(w -> w.equalsIgnoreCase(word)).
            findFirst().
            orElse(null);
    }

    /** Finds the next word in the given input not already added to the builder, and adds it
     *  after converting to title case. */
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

        non_title_case_words.add(word);
        return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
    }

    /** Tests whether given word has title case. */
    @SuppressWarnings("TypeMayBeWeakened")
    private static boolean isTitleCase(final String word) {

        return !Character.isLowerCase(word.charAt(0)) &&
            word.chars().boxed().skip(1).noneMatch(Character::isUpperCase);
    }

    /** For each map entry, searches for instances of the key in the given string (case insensitive)
     *  and replaces each one with the corresponding value. */
    private static String replaceAllMapEntries(final String s, final Map<String, String> normalisation_map) {

        String result = s;

        for (final Map.Entry<String, String> entry : normalisation_map.entrySet())
            // "(?i)" specifies case insensitive map lookup.
            result = result.replaceAll(STR."(?i)\{entry.getKey()}", entry.getValue());

        return result;
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
            throw new DateTimeParseException(time, time, 0);
        }
    }

    private static String formatWholePart(final Duration duration) {

        final long total_seconds = duration.getSeconds();

        final long hours = total_seconds / SECONDS_PER_HOUR;
        final long minutes = (total_seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE;
        final long seconds = total_seconds % SECONDS_PER_MINUTE;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private static String formatFractionalPart(final Duration duration) {

        final int fractional_seconds_as_nanoseconds = duration.getNano();
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

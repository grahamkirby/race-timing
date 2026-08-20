/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2026 Graham Kirby (race-timing@kirby-family.net)
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
package org.grahamkirby.race_timing.common;

import com.itextpdf.io.font.constants.StandardFonts;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Config {

    public static final int POSITION_INDEX = 1;
    public static final int BIB_INDEX = 2;
    public static final int TIME_INDEX = 3;
    public static final int COMMENT_INDEX = 4;

    public static final int INDENT = 24;

    public static final Duration DUMMY_WINNING_TIME = Duration.ofMinutes(10);
    public static final Duration DUMMY_INTERVAL = Duration.ofSeconds(12);

    public static final String CSV_FILE_SUFFIX = "csv";
    public static final String HTML_FILE_SUFFIX = "html";
    public static final String PDF_FILE_SUFFIX = "pdf";
    public static final String TEXT_FILE_SUFFIX = "txt";

    public static final String ILLEGAL_AGE_RANGE = "illegal age range";
    public static final String DUPLICATED_CATEGORY_NAME = "duplicated category name";
    public static final String GENDERS_ARE_NOT_CONSISTENT_BETWEEN_ENTRY_CATEGORIES = "genders are not consistent between entry categories";
    public static final String PRIZE_CATEGORIES = "prize categories";
    public static final String INVALID_INTERSECTING_AGE_RANGES = "invalid intersecting age ranges";
    public static final String INVALID_CATEGORIES_MISSING_AGE_RANGE_FOR = "invalid categories: missing age range for";
    public static final String TOO_FEW_CATEGORY_ELEMENTS = "too few category elements";
    public static final String INVALID_AGE_RANGE_FOR_CATEGORY = "invalid age range for category";
    public static final String TOO_FEW_CATEGORY_ELEMENTS1 = "too few category elements";
    public static final String INVALID_NUMBER_OF_PRIZES = "invalid number of prizes";
    public static final String ELIGIBLE_CLUBS_SEPARATOR = "/";
    public static final String NO_ENTRY_FOR_KEY = "no entry for key";
    public static final String INVALID_ENTRY = "invalid entry";
    public static final String SHOULD_HAVE_NO_KEYS_FROM = "should have no keys from";
    public static final String SHOULD_HAVE_NO_OR_ALL_KEYS_FROM = "should have no or all keys from";
    public static final String SHOULD_HAVE_NO_MORE_THAN_ONE_KEY_FROM = "should have no more than one key from";
    public static final String RUNNER_PAIR_NAMES_SEPARATOR = " & ";
    public static final String NO_RESULTS = "No results";
    public static final String AT_LINE = "at line";
    public static final String IN_FILE = "in file";
    public static final String DUPLICATE_BIB_NUMBER = "duplicate bib number";
    public static final String INVALID_RECORD = "invalid record";
    public static final String POSSIBLE_INVALID_USE_OF_COMMENT_SYMBOL = "possible invalid use of # comment symbol";
    public static final String INVALID_CATEGORY_IN_ENTRY = "invalid category in entry";
    public static final String AT_LINE1 = "result out of order at line";
    public static final String NO_APPLICABLE_RACE_TYPE_FOR_CONFIG_FILE = "no applicable race type for config file";
    public static final String CANNOT_CREATE_OUTPUT_DIRECTORY_OR_FILE_WITHIN_IT = "cannot create output directory, or file within it";
    public static final String OVERALL = "Overall";
    public static final String RUNNER = "Runner";
    public static final String UNKNOWN_CATEGORY_SO_OMITTED_FROM_OVERALL_RESULTS = "unknown category so omitted from overall results";
    public static final String PROCESSING_NOTES = "processing_notes";
    public static final String PRIZES = "Prizes";
    public static final String RESULTS = "Results";
    public static final String CURRENT_STANDINGS = "Current Standings";
    public static final String CATEGORY = "Category";
    public static final String CATEGORY_PRIZES = "Category Prizes";
    public static final String COMBINED = "combined";
    public static final String EQUAL_POSITION_INDICATOR = "=";
    public static final String POCKET_TIMER_RACERS_FILENAME = "racers";
    public static final String DUMMY_RAWTIMES_FILENAME = "dummy_rawtimes";
    public static final String CONVERTED_TO_TITLE_CASE = "Converted to title case";

    public static final String RAW_RESULT_SEPARATOR = "\t";
    public static final String RACE_ENTRY_SEPARATOR = "\t";
    public static final String CONFIG_INNER_SEPARATOR = "/";
    public static final String CONFIG_OUTER_SEPARATOR = ",";
    public static final String CSV_SEPARATOR = ",";

    public static final String INVALID_ENTRY1 = "invalid entry";
    public static final String FOR_KEY = "for key";
    public static final String FIRST = "First";
    public static final String TEAM = "team";
    public static final String TEAM_PRIZES = "Team Prizes";
    public static final String UNDERLINE = "-----------";
    public static final String COMBINED1 = "combined";
    public static final String BIB_NUMBER = "bib number";
    public static final String RECORDED_AS_DNF_BUT_NO_RESULT_WAS_RECORDED = "recorded as DNF but no result was recorded";
    public static final String TEAM_SCORES = "Team scores";
    public static final String UNATT = "Unatt.";
    public static final String ENTRY_SEPARATOR = "\t";
    public static final String UNREGISTERED_BIB_NUMBER = "unregistered bib number";
    public static final String AT_LINE2 = "at line";
    public static final String DUPLICATE_ENTRY = "duplicate entry";
    public static final String IN_FILE2 = "in file";
    public static final String SURPLUS_RESULT_FOR_TEAM = "surplus result for team";
    public static final String UPDATE = "Update";
    public static final String INVALID_NUMBER_OF_ELEMENTS = "invalid number of elements";
    public static final String MASS_START_INDICATOR = "M";
    public static final String FOR_KEY1 = "invalid mass start time for key";
    public static final String FOR_KEY2 = "invalid leg number for key";
    public static final String FOR_KEY3 = "invalid mass start time order for key";
    public static final String TIME = "Time";
    public static final String OVERALL_RESULTS_HEADER2 = "Pos,No,Team,Category,";
    public static final String DETAILED = "detailed";
    public static final String M_3_MASS_START_LEG_3 = "M3: mass start leg 3";
    public static final String M_4_MASS_START_LEG_4 = "M4: mass start leg 4";
    public static final String LEG = "leg_";
    public static final String LEG1 = "Leg";
    public static final String FULL_RESULTS = "Full Results";
    public static final String TIMES_COLLATED = "times_collated";
    public static final String TOTAL = "Total";
    public static final String POS = "Pos";
    public static final String RUNNERS = "Runners";
    public static final String SPLIT = "Split";

    public static final List<String> HEADERS = List.of(POS, "No", "Runner", "Club", "Category", "Time");
    public static final List<String> POS1 = List.of(POS, "No", "Team", "Category");
    public static final List<String> HEADERS2 = concat(POS1, List.of(TOTAL));


    public static final String TIME_NOT_RECORDED_NO_BASIS_FOR_INTERPOLATION_SO_SET_TO_FIRST_RECORDED_TIME = "Time not recorded. No basis for interpolation so set to first recorded time.";
    public static final String TIME_NOT_RECORDED_TIME_INTERPOLATED = "Time not recorded. Time interpolated.";
    public static final String TIME_NOT_RECORDED_NO_BASIS_FOR_INTERPOLATION_SO_SET_TO_LAST_RECORDED_TIME_1_S = "Time not recorded. No basis for interpolation so set to last recorded time + 1s.";
    public static final String TIME_BUT_NOT_BIB_NUMBER_RECORDED_ELECTRONICALLY_BIB_NUMBER_NOT_RECORDED_ON_PAPER_TOO_MANY_MISSING_TIMES_TO_GUESS_FROM_DNF_TEAMS = "Time but not bib number recorded electronically. Bib number not recorded on paper. Too many missing times to guess from DNF teams.";
    public static final String TIME_BUT_NOT_BIB_NUMBER_RECORDED_ELECTRONICALLY_BIB_NUMBER_NOT_RECORDED_ON_PAPER_GUESSED_BIB_NUMBER = "Time but not bib number recorded electronically. Bib number not recorded on paper. Guessed bib number.";
    public static final String INVALID_NUMBER_OF_RACES_SPECIFIED_IN_FILE = "invalid number of races specified in file";
    public static final String DUPLICATE_RACES_SPECIFIED_IN_FILE = "duplicate races specified in file";
    public static final String INVALID_CONFIG_FOR_RACE = "invalid config for race";
    public static final String RUNNERS_IN_SERIES = "Runners in Series";
    public static final String CLUB = "Club";
    public static final String SUBSTITUTED_FOR_UNKNOWN_CLUBS_FOR_RUNNER_NAME = "substituted for unknown clubs for runner name";
    public static final String RUNNER_NAME = "Runner name";
    public static final String RECORDED_FOR_MULTIPLE_CLUBS = "recorded for multiple clubs";
    public static final String ASSUMING_THERE_ARE_MULTIPLE_RUNNERS_WITH_THIS_NAME = "assuming there are multiple runners with this name";
    public static final String CATEGORY_CHANGES = "Category Changes";
    public static final String CHANGED_CATEGORY_FROM = "changed category from";
    public static final String TO = "to";
    public static final String AT = "at";
    public static final String INVALID_CATEGORY_CHANGE = "invalid category change";
    public static final String CHANGED_FROM = "changed from";
    public static final String DURING_SERIES = "during series";
    public static final String FINISHER_WAS_RUNNER = "finisher was runner";
    public static final String TO_FINISH_FOR_TEAM = "to finish for team";
    public static final String DISCREPANCIES = """
        
        Discrepancies:
        -------------
        """;
    public static final String S = """
        
        
        """;
    public static final String S1 = """
        
        Bib numbers with missing times:\s""";
    public static final String S2 = """
        
        Times with missing bib numbers:
        
        """;

    // Treated differently from other configurable paths, because it needs to be accessed
    // from test code independently of a particular race.
    public static final Path IGNORED_FILE_NAMES_PATH = Path.of("src/main/resources/configuration/ignored_file_names." + CSV_FILE_SUFFIX);

    public static final String KEY_ANNOTATIONS_PATH = "ANNOTATIONS_PATH";
    public static final String KEY_CAPITALISATION_STOP_WORDS_PATH = "CAPITALISATION_STOP_WORDS_PATH";
    public static final String KEY_CATEGORY_MAP_PATH = "CATEGORY_MAP_PATH";
    public static final String KEY_CHECK_INPUT_FILES_USED = "CHECK_INPUT_FILES_USED";
    public static final String KEY_DEAD_HEATS = "DEAD_HEATS";
    public static final String KEY_DNF_FINISHERS = "DNF_FINISHERS";
    public static final String KEY_ENTRIES_PATH = "ENTRIES_PATH";
    public static final String KEY_ENTRY_CATEGORIES_PATH = "ENTRY_CATEGORIES_PATH";
    public static final String KEY_ENTRY_COLUMN_MAP = "ENTRY_COLUMN_MAP";
    public static final String KEY_INDIVIDUAL_LEG_STARTS = "INDIVIDUAL_LEG_STARTS";
    public static final String KEY_MASS_START_TIMES = "MASS_START_TIMES";
    public static final String KEY_MEDIAN_TIME = "MEDIAN_TIME";
    public static final String KEY_MINIMUM_NUMBER_OF_RACES = "MINIMUM_NUMBER_OF_RACES";
    public static final String KEY_NORMALISED_CLUB_NAMES_PATH = "NORMALISED_CLUB_NAMES_PATH";
    public static final String KEY_NORMALISED_HTML_ENTITIES_PATH = "NORMALISED_HTML_ENTITIES_PATH";
    public static final String KEY_NUMBER_OF_LEGS = "NUMBER_OF_LEGS";
    public static final String KEY_NUMBER_OF_RACES_IN_SERIES = "NUMBER_OF_RACES_IN_SERIES";

    public static final String KEY_OFFSETS_CATEGORY_STARTS = "OFFSETS_CATEGORY_STARTS";

    // Offsets between the time that recording began, and the times that specified
    // runners started the race. A positive time value means that the runner started
    // after the main start; a negative time value means that the runner started early.
    // Specified as a comma-separated sequence of bib-number/offset pairs.
    // Example: OFFSETS_INDIVIDUAL_STARTS = 61/0:10:00
    public static final String KEY_OFFSETS_INDIVIDUAL_STARTS = "OFFSETS_INDIVIDUAL_STARTS";

    // Offset between the time that recording began, and the time that the race
    // actually started. A positive value means the race started after recording began.
    // Example: OFFSET_RACE_START = 00:01:00
    public static final String KEY_OFFSET_RACE_START = "OFFSET_RACE_START";

    public static final String KEY_OVERALL_RESULTS_PATH = "RESULTS_PATH";
    public static final String KEY_OUTPUT_ERRORS_TO_CONSOLE = "OUTPUT_ERRORS_TO_CONSOLE";
    public static final String KEY_PAIRED_LEGS = "PAIRED_LEGS";
    public static final String KEY_PAPER_RESULTS_PATH = "PAPER_RESULTS_PATH";
    public static final String KEY_PREFER_LOWER_PRIZE_IN_MORE_GENERAL_CATEGORY = "PREFER_LOWER_PRIZE_IN_MORE_GENERAL_CATEGORY";
    public static final String KEY_PRIZE_CATEGORIES_PATH = "PRIZE_CATEGORIES_PATH";
    public static final String KEY_ELIGIBLE_CLUBS = "ELIGIBLE_CLUBS";
    public static final String KEY_RACES = "RACES";
    public static final String KEY_RACE_NAME_FOR_FILENAMES = "RACE_NAME_FOR_FILENAMES";
    public static final String KEY_RACE_CATEGORIES_PATH = "RACE_CATEGORIES_PATH";
    public static final String KEY_RACE_NAME_FOR_RESULTS = "RACE_NAME_FOR_RESULTS";
    public static final String KEY_RACE_TEMPORAL_ORDER = "RACE_TEMPORAL_ORDER";
    public static final String KEY_RAW_RESULTS_PATH = "RAW_RESULTS_PATH";
    public static final String KEY_SCORE_FOR_FIRST_PLACE = "SCORE_FOR_FIRST_PLACE";
    public static final String KEY_SCORE_FOR_MEDIAN_POSITION = "SCORE_FOR_MEDIAN_POSITION";
    public static final String KEY_SEPARATELY_RECORDED_RESULTS = "SEPARATELY_RECORDED_RESULTS";
    public static final String KEY_TEAM_PRIZE_GENDER_CATEGORIES = "TEAM_PRIZE_GENDER_CATEGORIES";
    public static final String KEY_TEAM_PRIZE_NUMBER_TO_COUNT = "TEAM_PRIZE_NUMBER_TO_COUNT";
    public static final String KEY_TIME_TRIAL_INTER_WAVE_INTERVAL = "TIME_TRIAL_INTER_WAVE_INTERVAL";
    public static final String KEY_TIME_TRIAL_RUNNERS_PER_WAVE = "TIME_TRIAL_RUNNERS_PER_WAVE";
    public static final String KEY_YEAR = "YEAR";

    public static final List<String> REQUIRED_CONFIG_KEYS = List.of(
        KEY_YEAR,
        KEY_RACE_NAME_FOR_FILENAMES,
        KEY_RACE_NAME_FOR_RESULTS
    );

    /** Displayed in results for runners that did not complete the course. */
    public static final String DNF_STRING = "DNF";

    /** Comment symbol used within configuration files. */
    public static final String COMMENT_SYMBOL = "#";

    public static final String PDF_PRIZE_FONT_NAME = StandardFonts.HELVETICA;
    public static final String PDF_PRIZE_FONT_BOLD_NAME = StandardFonts.HELVETICA_BOLD;
    public static final String PDF_PRIZE_FONT_ITALIC_NAME = StandardFonts.HELVETICA_OBLIQUE;
    public static final int PDF_PRIZE_FONT_SIZE = 24;

    /** Platform-specific line separator used in creating output files. */
    public static final String LINE_SEPARATOR = System.lineSeparator();

    /** Used when a result is recorded without a bib number. */
    public static final String UNKNOWN_BIB_NUMBER_INDICATOR = "?";
    public static final int UNKNOWN_BIB_NUMBER = 0;

    public static final String UNKNOWN_TIME_INDICATOR = "?";
    public static final String UNKNOWN_CLUB_INDICATOR = "?";

    public static final OpenOption[] STANDARD_FILE_OPEN_OPTIONS = {StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE};

    /** Web link to application on GitHub. */
    public static final String SOFTWARE_CREDIT_LINK_TEXT = "<p style=\"font-size:smaller; font-style:italic;\">Results generated using <a href=\"https://github.com/grahamkirby/race-timing\">race-timing</a>.</p>";
    public static final String OUTPUT_DIRECTORY_NAME = "output";
    public static final String MISSING_CONFIG_FILE = "missing config file";
    public static final String UNUSED_KEYS = "unused keys";
    public static final String UNUSED_INPUT_FILES = "unused input files";

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private final Map<String, Object> config_map;
    private final List<String> unused_keys;
    private final List<Path> unused_files;

    private final Path config_path;
    private final List<ConfigProcessor> config_adjusters = new ArrayList<>();
    private final List<ConfigProcessor> config_validators = new ArrayList<>();

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public Config(final Path config_file_path) throws IOException {

        this.config_path = config_file_path;
        config_map = new HashMap<>();

        final Properties properties = loadProperties(config_file_path);

        properties.forEach((key, value) -> config_map.put((String) key, value));

        unused_keys = new ArrayList<>(config_map.keySet());
        unused_keys.removeAll(REQUIRED_CONFIG_KEYS);

        unused_files = makeMutableCopy(getInputFiles());
        unused_files.remove(config_path);
    }

    public static List<String> getIgnoredFileNames() throws IOException {

        return readAllLines(IGNORED_FILE_NAMES_PATH).stream().
            map(NormalisationProcessor::stripComment).
            filter(Predicate.not(String::isBlank)).
            toList();
    }

    public static <T> List<T> makeMutableCopy(final List<T> list) {

        return new ArrayList<>(list);
    }

    public static List<String> readAllLines(final Path path) throws IOException {

        return path == null ? List.of() : Files.readAllLines(path);
    }

    public static Properties loadProperties(final Path config_file_path) throws IOException {

        if (!Files.exists(config_file_path))
            throw new RuntimeException(MISSING_CONFIG_FILE + ": '" + config_file_path + "'");

        try (final InputStreamReader reader = new InputStreamReader(Files.newInputStream(config_file_path), StandardCharsets.UTF_8)) {

            final Properties properties = new Properties();
            properties.load(reader);
            return properties;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public void checkUnusedProperties() {

        if (!unused_keys.isEmpty())
            throw new RuntimeException(UNUSED_KEYS + ": " + String.join(", ", unused_keys));
    }

    public void checkUnusedInputFiles() throws IOException {

        getIgnoredFileNames().forEach(ignored_file_name -> unused_files.remove(config_path.getParent().resolve(ignored_file_name)));

        if (!unused_files.isEmpty() && ((Boolean) get(KEY_CHECK_INPUT_FILES_USED))) {

            final String message = UNUSED_INPUT_FILES + ": " +
                unused_files.stream().
                    map(path -> path.getFileName().toString()).
                    collect(Collectors.joining(", ")) + LINE_SEPARATOR;

            throw new RuntimeException(message);
        }
    }

    public Object get(final String key) {

        unused_keys.remove(key);
        return config_map.get(key);
    }

    public String getString(final String key) {

        return (String) get(key);
    }

    public Path getPath(final String key) {

        final Path path = (Path) get(key);

        if (path != null)
            unused_files.remove(path.normalize());

        return path;
    }

    public boolean containsKey(final String key) {
        return config_map.containsKey(key);
    }

    public Path getConfigPath() {
        return config_path;
    }

    public void addIfAbsent(final String key, final Object value) {

        config_map.putIfAbsent(key, value);
    }

    public void processConfigIfPresent(final String key, final Consumer<Object> processor) {

        if (config_map.containsKey(key))
            processor.accept(get(key));
    }

    public void replace(final String key, final Function<String, Object> make_new_value) {

        config_map.replace(key, make_new_value.apply((String) config_map.get(key)));
    }

    public void replace(final String key, final Object new_value) {

        config_map.replace(key, new_value);
    }

    public void replaceIfPresent(final String key, final Function<String, Object> make_new_value) {

        if (config_map.containsKey(key))
            config_map.replace(key, make_new_value.apply((String) config_map.get(key)));
    }

    public void replaceIfPresent(final List<String> keys, final Function<String, Object> make_new_value) {

        for (final String key : keys)
            replaceIfPresent(key, make_new_value);
    }

    public void addConfigAdjuster(final Function<Config, ConfigProcessor> make_processor) {

        config_adjusters.add(make_processor.apply(this));
    }

    public void addConfigValidator(final Function<Config, ConfigProcessor> make_processor) {

        config_validators.add(make_processor.apply(this));
    }

    public void processConfigAdjusters() {

        for (final ConfigProcessor processor : config_adjusters)
            processor.processConfig();
    }

    public void processConfigValidators() {

        for (final ConfigProcessor processor : config_validators)
            processor.processConfig();
    }

    /**
     * Resolves the given path relative to either the race configuration file,
     * if it's specified as a relative path, or to the project root. Examples:
     *
     * Relative to race configuration:
     * entries.txt -> /Users/gnck/Desktop/myrace/input/entries.txt
     *
     * Relative to project root:
     * /src/main/resources/configuration/categories_entry_individual_senior.csv ->
     *    src/main/resources/configuration/categories_entry_individual_senior.csv
     */
    @SuppressWarnings("JavadocBlankLines")
    public Path interpretPath(final String path_as_string) {

        final Path path = Path.of(path_as_string);

        // .isAbsolute() won't work for this check on Windows since an absolute path may originate from config file,
        // where "/" is used on all platforms. Such a path denotes a path relative to the project root.
        if (path_as_string.startsWith(File.separator) || path_as_string.startsWith("/"))
            return makeRelative(path);
        else
            // Path is a relative path from the directory containing the config file.
            return getAbsolutePath(path);
    }

    public Path getOutputDirectoryPath() {

        // This assumes that the config file is in the "input" directory
        // which is at the same level as the "output" directory.

        return config_path.getParent().resolveSibling(OUTPUT_DIRECTORY_NAME);
    }

    public String getRaceName() {

        return NormalisationProcessor.cleanSpacesAndQuotes(getString(KEY_RACE_NAME_FOR_RESULTS));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static Path makeRelative(final Path path) {

        // Convert absolute path to relative path from the project directory,
        // interpreting the input path's root as the project directory.
        return Path.of(path.toString().substring(1));
    }

    private Path getAbsolutePath(final Path relative_path_from_config_directory) {

        return config_path.resolveSibling(relative_path_from_config_directory);
    }

    private List<Path> getInputFiles() throws IOException {

        final Path input_directory = config_path.getParent();

        try (final Stream<Path> paths = Files.list(input_directory)) {
            return paths.filter(Files::isRegularFile).toList();
        }
    }

    private static List<String> concat(final List<String> list1, final List<String> list2) {
        return Stream.concat(list1.stream(), list2.stream()).toList();
    }
}


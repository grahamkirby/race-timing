/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (race-timing@kirby-family.net)
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class Config {

    public static final String CSV_FILE_SUFFIX = "csv";
    public static final String HTML_FILE_SUFFIX = "html";
    public static final String PDF_FILE_SUFFIX = "pdf";
    public static final String TEXT_FILE_SUFFIX = "txt";

    public static final Path DEFAULT_CONFIG_ROOT_PATH = Path.of("/src/main/resources/configuration");

    public static final String KEY_ANNOTATIONS_PATH = "ANNOTATIONS_PATH";
    public static final String KEY_CAPITALISATION_STOP_WORDS_PATH = "CAPITALISATION_STOP_WORDS_PATH";
    public static final String KEY_CATEGORY_MAP_PATH = "CATEGORY_MAP_PATH";
    public static final String KEY_CATEGORY_START_OFFSETS = "CATEGORY_START_OFFSETS";
    public static final String KEY_DNF_FINISHERS = "DNF_FINISHERS";
    public static final String KEY_ENTRIES_PATH = "ENTRIES_PATH";
    public static final String KEY_ENTRY_CATEGORIES_PATH = "ENTRY_CATEGORIES_PATH";
    public static final String KEY_ENTRY_COLUMN_MAP = "ENTRY_COLUMN_MAP";
    public static final String KEY_GENDER_ELIGIBILITY_MAP_PATH = "GENDER_ELIGIBILITY_MAP_PATH";
    public static final String KEY_INDIVIDUAL_LEG_STARTS = "INDIVIDUAL_LEG_STARTS";
    public static final String KEY_INDIVIDUAL_START_TIMES = "INDIVIDUAL_START_TIMES";
    public static final String KEY_MASS_START_TIMES = "MASS_START_TIMES";
    public static final String KEY_MEDIAN_TIME = "MEDIAN_TIME";
    public static final String KEY_MINIMUM_NUMBER_OF_RACES = "MINIMUM_NUMBER_OF_RACES";
    public static final String KEY_NORMALISED_CLUB_NAMES_PATH = "NORMALISED_CLUB_NAMES_PATH";
    public static final String KEY_NORMALISED_HTML_ENTITIES_PATH = "NORMALISED_HTML_ENTITIES_PATH";
    public static final String KEY_NUMBER_OF_LEGS = "NUMBER_OF_LEGS";
    public static final String KEY_NUMBER_OF_RACES_IN_SERIES = "NUMBER_OF_RACES_IN_SERIES";
    public static final String KEY_NUMBER_TO_COUNT_FOR_TEAM_PRIZE = "NUMBER_TO_COUNT_FOR_TEAM_PRIZE";
    public static final String KEY_PAIRED_LEGS = "PAIRED_LEGS";
    public static final String KEY_PAPER_RESULTS_PATH = "PAPER_RESULTS_PATH";
    public static final String KEY_PRIZE_CATEGORIES_PATH = "PRIZE_CATEGORIES_PATH";
    public static final String KEY_QUALIFYING_CLUBS = "QUALIFYING_CLUBS";
    public static final String KEY_RACES = "RACES";
    public static final String KEY_RACE_NAME_FOR_FILENAMES = "RACE_NAME_FOR_FILENAMES";
    public static final String KEY_RACE_CATEGORIES_PATH = "RACE_CATEGORIES_PATH";
    public static final String KEY_RACE_NAME_FOR_RESULTS = "RACE_NAME_FOR_RESULTS";
    public static final String KEY_RACE_START_TIME = "RACE_START_TIME";
    public static final String KEY_RACE_TEMPORAL_ORDER = "RACE_TEMPORAL_ORDER";
    public static final String KEY_RAW_RESULTS_PATH = "RAW_RESULTS_PATH";
    public static final String KEY_RESULTS_PATH = "RESULTS_PATH";
    public static final String KEY_SCORE_FOR_FIRST_PLACE = "SCORE_FOR_FIRST_PLACE";
    public static final String KEY_SCORE_FOR_MEDIAN_POSITION = "SCORE_FOR_MEDIAN_POSITION";
    public static final String KEY_SEPARATELY_RECORDED_RESULTS = "SEPARATELY_RECORDED_RESULTS";
    public static final String KEY_TIME_TRIAL_INTER_WAVE_INTERVAL = "TIME_TRIAL_INTER_WAVE_INTERVAL";
    public static final String KEY_TIME_TRIAL_RUNNERS_PER_WAVE = "TIME_TRIAL_RUNNERS_PER_WAVE";
    public static final String KEY_YEAR = "YEAR";

    /** Displayed in results for runners that did not complete the course. */
    public static final String DNF_STRING = "DNF";

    /** Comment symbol used within configuration files. */
    public static final String COMMENT_SYMBOL = "#";

    public static final String PDF_PRIZE_FONT_NAME = StandardFonts.HELVETICA;
    public static final String PDF_PRIZE_FONT_BOLD_NAME = StandardFonts.HELVETICA_BOLD;
    public static final String PDF_PRIZE_FONT_ITALIC_NAME = StandardFonts.HELVETICA_OBLIQUE;
    public static final int PDF_PRIZE_FONT_SIZE = 24;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Platform-specific line separator used in creating output files. */
    public static final String LINE_SEPARATOR = System.lineSeparator();
    public static final String PATH_SEPARATOR = File.separator;

    /** Used when a result is recorded without a bib number. */
    public static final String UNKNOWN_BIB_NUMBER_INDICATOR = "?";
    public static final int UNKNOWN_BIB_NUMBER = 0;

    public static final String UNKNOWN_TIME_INDICATOR = "?";
    public static final String UNKNOWN_CLUB_INDICATOR = "?";

    public static final OpenOption[] STANDARD_FILE_OPEN_OPTIONS = {StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE};

    /** Web link to application on GitHub. */
    public static final String SOFTWARE_CREDIT_LINK_TEXT = "<p style=\"font-size:smaller; font-style:italic;\">Results generated using <a href=\"https://github.com/grahamkirby/race-timing\">race-timing</a>.</p>";

    private final Map<String, Object> config_map;
    private final List<String> unused_keys;

    private final Path config_path;

    public Config(final Path config_file_path) throws IOException {

        this.config_path = config_file_path;
        config_map = new HashMap<>();

        final Properties properties = loadProperties(config_file_path);

        properties.forEach((key, value) -> config_map.put((String) key, value));
        unused_keys = new ArrayList<>(config_map.keySet());
    }

    /** Encodes a single value by surrounding with quotes if it contains a comma. */
    public static String encode(final String s) {
        return s.contains(",") ? "\"" + s + "\"" : s;
    }

    public static Properties loadProperties(final Path config_file_path) throws IOException {

        if (!Files.exists(config_file_path))
            throw new RuntimeException("missing config file: '" + config_file_path + "'");

        try (final InputStream stream = Files.newInputStream(config_file_path)) {

            final Properties properties = new Properties();
            properties.load(stream);
            return properties;
        }
    }

    public void outputUnusedProperties() {

        if (!unused_keys.isEmpty()) {

            System.out.println("\nUnused keys: " + config_path);
            unused_keys.forEach(System.out::println);
            throw new RuntimeException("Unused keys in: " + config_path);
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

        return (Path) get(key);
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

    public void replaceIfPresent(final String key, final Function<String, Object> make_new_value) {

        if (config_map.containsKey(key))
            config_map.replace(key, make_new_value.apply((String) config_map.get(key)));
    }

    public void replaceIfPresent(final List<String> keys, final Function<String, Object> make_new_value) {

        for (final String key : keys)
            replaceIfPresent(key, make_new_value);
    }

    private final List<ConfigProcessor> config_processors = new ArrayList<>();

    public void addConfigProcessor(final Function<Config, ConfigProcessor> make_processor) {

        config_processors.add(make_processor.apply(this));
    }

    public void processConfig() throws IOException {

        for (final ConfigProcessor processor : config_processors)
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
    public Path interpretPath(final Path path) {

        // Absolute paths originate from config file where path starting with "/" denotes
        // a path relative to the project root.
        // Can't test with isAbsolute() since that will return false on Windows.
        if (path.startsWith("/")) return makeRelativeToProjectRoot(path);

        return getPathRelativeToRaceConfigFile(path);
    }

    private static Path makeRelativeToProjectRoot(final Path path) {

        // Path is specified as absolute path, should be reinterpreted relative to project root.
        return path.subpath(0, path.getNameCount());
    }

    private Path getPathRelativeToRaceConfigFile(final Path path) {

        return config_path.resolveSibling(path);
    }

    public Path getOutputDirectoryPath() {

        // This assumes that the config file is in the "input" directory
        // which is at the same level as the "output" directory.
        return config_path.getParent().resolveSibling("output");
    }
}


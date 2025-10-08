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
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

public class Config {

    public static String CSV_FILE_SUFFIX = "csv";
    public static String HTML_FILE_SUFFIX = "html";
    public static String PDF_FILE_SUFFIX = "pdf";
    public static String TEXT_FILE_SUFFIX = "txt";

    public static Path DEFAULT_CONFIG_ROOT_PATH = Path.of("/src/main/resources/configuration");

    public static String KEY_ANNOTATIONS_PATH = "ANNOTATIONS_PATH";
    public static String KEY_CAPITALISATION_STOP_WORDS_PATH = "CAPITALISATION_STOP_WORDS_PATH";
    public static String KEY_CATEGORY_MAP_PATH = "CATEGORY_MAP_PATH";
    public static String KEY_CATEGORY_START_OFFSETS = "CATEGORY_START_OFFSETS";
    public static String KEY_DNF_FINISHERS = "DNF_FINISHERS";
    public static String KEY_ENTRIES_PATH = "ENTRIES_PATH";
    public static String KEY_ENTRY_CATEGORIES_PATH = "ENTRY_CATEGORIES_PATH";
    public static String KEY_ENTRY_COLUMN_MAP = "ENTRY_COLUMN_MAP";
    public static String KEY_GENDER_ELIGIBILITY_MAP_PATH = "GENDER_ELIGIBILITY_MAP_PATH";
    public static String KEY_INDIVIDUAL_EARLY_STARTS = "INDIVIDUAL_EARLY_STARTS";
    public static String KEY_INDIVIDUAL_LEG_STARTS = "INDIVIDUAL_LEG_STARTS";
    public static String KEY_MASS_START_ELAPSED_TIMES = "MASS_START_ELAPSED_TIMES";
    public static String KEY_MEDIAN_TIME = "MEDIAN_TIME";
    public static String KEY_MINIMUM_NUMBER_OF_RACES = "MINIMUM_NUMBER_OF_RACES";
    public static String KEY_NORMALISED_CLUB_NAMES_PATH = "NORMALISED_CLUB_NAMES_PATH";
    public static String KEY_NORMALISED_HTML_ENTITIES_PATH = "NORMALISED_HTML_ENTITIES_PATH";
    public static String KEY_NUMBER_OF_LEGS = "NUMBER_OF_LEGS";
    public static String KEY_NUMBER_OF_RACES_IN_SERIES = "NUMBER_OF_RACES_IN_SERIES";
    public static String KEY_NUMBER_TO_COUNT_FOR_TEAM_PRIZE = "NUMBER_TO_COUNT_FOR_TEAM_PRIZE";
    public static String KEY_PAIRED_LEGS = "PAIRED_LEGS";
    public static String KEY_PAPER_RESULTS_PATH = "PAPER_RESULTS_PATH";
    public static String KEY_PRIZE_CATEGORIES_PATH = "PRIZE_CATEGORIES_PATH";
    public static String KEY_QUALIFYING_CLUBS = "QUALIFYING_CLUBS";
    public static String KEY_RACES = "RACES";
    public static String KEY_RACE_NAME_FOR_FILENAMES = "RACE_NAME_FOR_FILENAMES";
    public static String KEY_RACE_CATEGORIES_PATH = "RACE_CATEGORIES_PATH";
    public static String KEY_RACE_NAME_FOR_RESULTS = "RACE_NAME_FOR_RESULTS";
    public static String KEY_RACE_TEMPORAL_ORDER = "RACE_TEMPORAL_ORDER";
    public static String KEY_RAW_RESULTS_PATH = "RAW_RESULTS_PATH";
    public static String KEY_RESULTS_PATH = "RESULTS_PATH";
    public static String KEY_SCORE_FOR_FIRST_PLACE = "SCORE_FOR_FIRST_PLACE";
    public static String KEY_SCORE_FOR_MEDIAN_POSITION = "SCORE_FOR_MEDIAN_POSITION";
    public static String KEY_SEPARATELY_RECORDED_RESULTS = "SEPARATELY_RECORDED_RESULTS";
    public static String KEY_START_OFFSET = "START_OFFSET";
    public static String KEY_TIME_TRIAL_INTER_WAVE_INTERVAL = "TIME_TRIAL_INTER_WAVE_INTERVAL";
    public static String KEY_TIME_TRIAL_RUNNERS_PER_WAVE = "TIME_TRIAL_RUNNERS_PER_WAVE";
    public static String KEY_TIME_TRIAL_STARTS = "TIME_TRIAL_STARTS";
    public static String KEY_YEAR = "YEAR";

    /** Displayed in results for runners that did not complete the course. */
    public static String DNF_STRING = "DNF";

    /** Comment symbol used within configuration files. */
    public static String COMMENT_SYMBOL = "#";

    public static String PDF_PRIZE_FONT_NAME = StandardFonts.HELVETICA;
    public static String PDF_PRIZE_FONT_BOLD_NAME = StandardFonts.HELVETICA_BOLD;
    public static String PDF_PRIZE_FONT_ITALIC_NAME = StandardFonts.HELVETICA_OBLIQUE;
    public static int PDF_PRIZE_FONT_SIZE = 24;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Platform-specific line separator used in creating output files. */
    public static String LINE_SEPARATOR = System.lineSeparator();
    public static String PATH_SEPARATOR = File.separator;

    /** Used when a result is recorded without a bib number. */
    public static int UNKNOWN_BIB_NUMBER = 0;

    public static final Duration VERY_LONG_DURATION = Duration.ofDays(Integer.MAX_VALUE);

    public static OpenOption[] STANDARD_FILE_OPEN_OPTIONS = {StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE};
    public static OpenOption[] APPEND_FILE_OPEN_OPTIONS = {StandardOpenOption.APPEND, StandardOpenOption.WRITE};

    /** Web link to application on GitHub. */
    public static String SOFTWARE_CREDIT_LINK_TEXT = "<p style=\"font-size:smaller; font-style:italic;\">Results generated using <a href=\"https://github.com/grahamkirby/race-timing\">race-timing</a>.</p>";

    private final Map<String, Object> config_map;
    private final Path config_path;

    public Config(final Path config_file_path) throws IOException {

        this.config_path = config_file_path;
        config_map = new HashMap<>();

        Properties properties = loadProperties(config_file_path);

        properties.forEach((key, value) -> config_map.put((String) key, value));
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

    public Object get(final String key) {
        return config_map.get(key);
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

    public void replace(final String key, final Function<String, Object> make_new_value) {

        config_map.replace(key, make_new_value.apply((String) config_map.get(key)));
    }

    public void replaceIfPresent(final String key, final Function<String, Object> make_new_value) {

        if (config_map.containsKey(key))
            config_map.replace(key, make_new_value.apply((String) config_map.get(key)));
    }

    public void replaceIfPresent(final List<String> keys, final Function<String, Object> make_new_value) {

        for (String key : keys)
            replaceIfPresent(key, make_new_value);
    }
}


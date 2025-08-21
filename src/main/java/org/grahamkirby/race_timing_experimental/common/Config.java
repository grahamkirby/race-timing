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

import com.itextpdf.io.font.constants.StandardFonts;

import java.io.File;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;

import static org.grahamkirby.race_timing_experimental.common.Normalisation.format;

public interface Config {

    String CSV_FILE_SUFFIX = "csv";
    String HTML_FILE_SUFFIX = "html";
    String PDF_FILE_SUFFIX = "pdf";
    String TEXT_FILE_SUFFIX = "txt";

    Path DEFAULT_CONFIG_ROOT_PATH = Path.of("/src/main/resources/configuration");

    Path DEFAULT_CAPITALISATION_STOP_WORDS_PATH = Path.of(STR."\{DEFAULT_CONFIG_ROOT_PATH}/capitalisation_stop_words.\{CSV_FILE_SUFFIX}");
    Path DEFAULT_NORMALISED_HTML_ENTITIES_PATH = Path.of(STR."\{DEFAULT_CONFIG_ROOT_PATH}/html_entities.\{CSV_FILE_SUFFIX}");
    Path DEFAULT_NORMALISED_CLUB_NAMES_PATH = Path.of(STR."\{DEFAULT_CONFIG_ROOT_PATH}/club_names.\{CSV_FILE_SUFFIX}");
    Path DEFAULT_GENDER_ELIGIBILITY_MAP_PATH = Path.of(STR."\{DEFAULT_CONFIG_ROOT_PATH}/gender_eligibility_default.\{CSV_FILE_SUFFIX}");

    String KEY_ANNOTATIONS_PATH = "ANNOTATIONS_PATH";
    String KEY_CAPITALISATION_STOP_WORDS_PATH = "CAPITALISATION_STOP_WORDS_PATH";
    String KEY_CATEGORY_MAP_PATH = "CATEGORY_MAP_PATH";
    String KEY_DNF_FINISHERS = "DNF_FINISHERS";
    String KEY_ENTRIES_PATH = "ENTRIES_PATH";
    String KEY_ENTRY_CATEGORIES_PATH = "ENTRY_CATEGORIES_PATH";
    String KEY_ENTRY_COLUMN_MAP = "ENTRY_COLUMN_MAP";
    String KEY_GENDER_ELIGIBILITY_MAP_PATH = "GENDER_ELIGIBILITY_MAP_PATH";
    String KEY_INDIVIDUAL_EARLY_STARTS = "INDIVIDUAL_EARLY_STARTS";
    String KEY_INDIVIDUAL_LEG_STARTS = "INDIVIDUAL_LEG_STARTS";
    String KEY_MASS_START_ELAPSED_TIMES = "MASS_START_ELAPSED_TIMES";
    String KEY_MEDIAN_TIME = "MEDIAN_TIME";
    String KEY_MINIMUM_NUMBER_OF_RACES = "MINIMUM_NUMBER_OF_RACES";
    String KEY_NORMALISED_CLUB_NAMES_PATH = "NORMALISED_CLUB_NAMES_PATH";
    String KEY_NORMALISED_HTML_ENTITIES_PATH = "NORMALISED_HTML_ENTITIES_PATH";
    String KEY_NUMBER_OF_LEGS = "NUMBER_OF_LEGS";
    String KEY_NUMBER_OF_RACES_IN_SERIES = "NUMBER_OF_RACES_IN_SERIES";
    String KEY_NUMBER_TO_COUNT_FOR_TEAM_PRIZE = "NUMBER_TO_COUNT_FOR_TEAM_PRIZE";
    String KEY_PAIRED_LEGS = "PAIRED_LEGS";
    String KEY_PAPER_RESULTS_PATH = "PAPER_RESULTS_PATH";
    String KEY_PRIZE_CATEGORIES_PATH = "PRIZE_CATEGORIES_PATH";
    String KEY_QUALIFYING_CLUBS = "QUALIFYING_CLUBS";
    String KEY_RACES = "RACES";
    String KEY_RACE_NAME_FOR_FILENAMES = "RACE_NAME_FOR_FILENAMES";
    String KEY_RACE_CATEGORIES_PATH = "RACE_CATEGORIES_PATH";
    String KEY_RACE_NAME_FOR_RESULTS = "RACE_NAME_FOR_RESULTS";
    String KEY_RACE_TEMPORAL_ORDER = "RACE_TEMPORAL_ORDER";
    String KEY_RAW_RESULTS_PATH = "RAW_RESULTS_PATH";
    String KEY_RESULTS_PATH = "RESULTS_PATH";
    String KEY_SCORE_FOR_FIRST_PLACE = "SCORE_FOR_FIRST_PLACE";
    String KEY_SCORE_FOR_MEDIAN_POSITION = "SCORE_FOR_MEDIAN_POSITION";
    String KEY_SECOND_WAVE_CATEGORIES = "SECOND_WAVE_CATEGORIES";
    String KEY_SELF_TIMED = "SELF_TIMED";
    String KEY_START_OFFSET = "START_OFFSET";
    String KEY_TIME_TRIAL_RACE = "TIME_TRIAL_RACE";
    String KEY_TIME_TRIAL_STARTS = "TIME_TRIAL_STARTS";
    String KEY_WAVE_START_OFFSETS = "WAVE_START_OFFSETS";
    String KEY_YEAR = "YEAR";

    /** Displayed in results for runners that did not complete the course. */
    String DNF_STRING = "DNF";

    /** Comment symbol used within configuration files. */
    String COMMENT_SYMBOL = "#";

    String PDF_PRIZE_FONT_NAME = StandardFonts.HELVETICA;
    String PDF_PRIZE_FONT_BOLD_NAME = StandardFonts.HELVETICA_BOLD;
    String PDF_PRIZE_FONT_ITALIC_NAME = StandardFonts.HELVETICA_OBLIQUE;
    int PDF_PRIZE_FONT_SIZE = 24;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Platform-specific line separator used in creating output files. */
    String LINE_SEPARATOR = System.lineSeparator();
    String PATH_SEPARATOR = File.separator;

    /** Used when a result is recorded without a bib number. */
    int UNKNOWN_BIB_NUMBER = 0;

    Duration NO_MASS_START_DURATION = Duration.ofSeconds(Long.MAX_VALUE);

    OpenOption[] STANDARD_FILE_OPEN_OPTIONS = {StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE};

    Object get(String key);

    /** Encodes a single value by surrounding with quotes if it contains a comma. */
    static String encode(final String s) {
        return s.contains(",") ? STR."\"\{s}\"" : s;
    }

    static String renderDuration(final Duration duration, final String alternative) {

        return duration != null ? format(duration) : alternative;
    }

    static String renderDuration(final RaceResult r, final String alternative) {

        SingleRaceResult result = (SingleRaceResult) r;
        if (!result.canComplete()) return alternative;

        return format(result.duration());
    }
}

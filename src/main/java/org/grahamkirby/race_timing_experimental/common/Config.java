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

import java.nio.file.Path;

public interface Config {

    String SUFFIX_CSV = ".csv";
    Path DEFAULT_CONFIG_ROOT_PATH = Path.of("/src/main/resources/configuration");

    String KEY_CAPITALISATION_STOP_WORDS_PATH = "CAPITALISATION_STOP_WORDS_PATH";
    String KEY_NORMALISED_CLUB_NAMES_PATH = "NORMALISED_CLUB_NAMES_PATH";
    String KEY_NORMALISED_HTML_ENTITIES_PATH = "NORMALISED_HTML_ENTITIES_PATH";
    String KEY_YEAR = "YEAR";
    String KEY_RACE_NAME_FOR_RESULTS = "RACE_NAME_FOR_RESULTS";
    String KEY_RACE_NAME_FOR_FILENAMES = "RACE_NAME_FOR_FILENAMES";
    String KEY_CATEGORIES_ENTRY_PATH = "CATEGORIES_ENTRY_PATH";
    String KEY_CATEGORIES_PRIZE_PATH = "CATEGORIES_PRIZE_PATH";
    String KEY_RESULTS_PATH = "RESULTS_PATH";
    String KEY_DNF_FINISHERS = "DNF_FINISHERS";
    String KEY_MEDIAN_TIME = "MEDIAN_TIME";
    String KEY_ENTRIES_PATH = "ENTRIES_PATH";
    String KEY_RAW_RESULTS_PATH = "RAW_RESULTS_PATH";
    String KEY_INDIVIDUAL_EARLY_STARTS = "INDIVIDUAL_EARLY_STARTS";
    String KEY_CATEGORY_MAP_PATH = "CATEGORY_MAP_PATH";
    String KEY_ENTRY_COLUMN_MAP = "ENTRY_COLUMN_MAP";
    String KEY_GENDER_ELIGIBILITY_MAP_PATH = "GENDER_ELIGIBILITY_MAP_PATH";
    String KEY_PAPER_RESULTS_PATH = "PAPER_RESULTS_PATH";
    String KEY_ANNOTATIONS_PATH = "ANNOTATIONS_PATH";
    String KEY_NUMBER_OF_LEGS = "NUMBER_OF_LEGS";
    String KEY_PAIRED_LEGS = "PAIRED_LEGS";
    String KEY_INDIVIDUAL_LEG_STARTS = "INDIVIDUAL_LEG_STARTS";
    String KEY_MASS_START_ELAPSED_TIMES = "MASS_START_ELAPSED_TIMES";
    String KEY_START_OFFSET = "START_OFFSET";

    Object get(String key);
}

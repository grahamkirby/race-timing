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
package org.grahamkirby.race_timing_experimental.individual_race;

import org.grahamkirby.race_timing_experimental.common.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.grahamkirby.race_timing.common.Race.loadProperties;
import static org.grahamkirby.race_timing_experimental.common.CommonConfigProcessor.*;
import static org.grahamkirby.race_timing_experimental.common.Config.*;
import static org.grahamkirby.race_timing_experimental.common.Normalisation.parseTime;

@SuppressWarnings("preview")
public class IndividualRaceConfigProcessor implements ConfigProcessor {

    private Race race;

    public void setRace(Race race) {
        this.race = race;
    }

    public String dnf_string;

    private static final List<String> REQUIRED_STRING_PROPERTY_KEYS =
        List.of(KEY_YEAR, KEY_RACE_NAME_FOR_RESULTS, KEY_RACE_NAME_FOR_FILENAMES);

    private static final List<String> OPTIONAL_STRING_PROPERTY_KEYS =
        List.of(KEY_DNF_FINISHERS, KEY_INDIVIDUAL_EARLY_STARTS, KEY_MEDIAN_TIME, KEY_CATEGORY_START_OFFSETS, KEY_SEPARATELY_RECORDED_RESULTS, KEY_TIME_TRIAL_STARTS);

    private static final List<String> REQUIRED_PATH_PROPERTY_KEYS =
        List.of(KEY_ENTRY_CATEGORIES_PATH, KEY_PRIZE_CATEGORIES_PATH);

    private static final List<String> OPTIONAL_PATH_PROPERTY_KEYS =
        List.of(KEY_CATEGORY_MAP_PATH, KEY_ENTRIES_PATH, KEY_RAW_RESULTS_PATH, KEY_RESULTS_PATH);

    private static final List<String> OPTIONAL_PATH_WITH_DEFAULT_PROPERTY_KEYS =
        List.of(KEY_CAPITALISATION_STOP_WORDS_PATH, KEY_NORMALISED_HTML_ENTITIES_PATH, KEY_NORMALISED_CLUB_NAMES_PATH, KEY_GENDER_ELIGIBILITY_MAP_PATH);

    private static final List<Path> OPTIONAL_PATH_DEFAULT_PROPERTIES =
        List.of(DEFAULT_CAPITALISATION_STOP_WORDS_PATH, DEFAULT_NORMALISED_HTML_ENTITIES_PATH, DEFAULT_NORMALISED_CLUB_NAMES_PATH, DEFAULT_GENDER_ELIGIBILITY_MAP_PATH);

    @Override
    public Config loadConfig(final Path config_file_path) {

        try {
            final Properties properties = loadProperties(config_file_path);

            CommonConfigProcessor commonConfigProcessor = new CommonConfigProcessor(race, config_file_path, properties);

            commonConfigProcessor.addRequiredStringProperties(REQUIRED_STRING_PROPERTY_KEYS);
            commonConfigProcessor.addOptionalStringProperties(OPTIONAL_STRING_PROPERTY_KEYS);

            commonConfigProcessor.addRequiredPathProperties(REQUIRED_PATH_PROPERTY_KEYS);
            commonConfigProcessor.addOptionalPathProperties(OPTIONAL_PATH_PROPERTY_KEYS);
            commonConfigProcessor.addOptionalPathProperties(OPTIONAL_PATH_WITH_DEFAULT_PROPERTY_KEYS, OPTIONAL_PATH_DEFAULT_PROPERTIES);

            // Default entry map with 4 elements (bib number, full name, club, category), and no column combining or re-ordering.
            commonConfigProcessor.addOptionalProperty(KEY_ENTRY_COLUMN_MAP, s -> s, _ -> makeDefaultEntryColumnMap(4));

            commonConfigProcessor.addOptionalProperty(KEY_NUMBER_TO_COUNT_FOR_TEAM_PRIZE, Integer::parseInt, _ -> Integer.MAX_VALUE);
            commonConfigProcessor.addOptionalProperty(KEY_TIME_TRIAL_RUNNERS_PER_WAVE, Integer::parseInt);
            commonConfigProcessor.addOptionalProperty(KEY_TIME_TRIAL_INTER_WAVE_INTERVAL, Normalisation::parseTime);

            final Map<String, Object> config_values = commonConfigProcessor.getConfigValues();

            if (!config_values.containsKey(KEY_RAW_RESULTS_PATH) && !config_values.containsKey(KEY_RESULTS_PATH)) {
                throw new RuntimeException(STR."no entry for either of keys '\{KEY_RAW_RESULTS_PATH}' or '\{KEY_RESULTS_PATH}' in file '\{config_file_path.getFileName()}'");
            }

            // Each DNF string contains single bib number.
            //noinspection ResultOfMethodCallIgnored
            validateDNFRecords((String) config_values.get(KEY_DNF_FINISHERS), Integer::parseInt, config_file_path);

            validateEntryCategoriesPath(config_values, config_file_path);
            validatePrizeCategoriesPath(config_values, config_file_path);

            return new ConfigImpl(config_values, config_file_path);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

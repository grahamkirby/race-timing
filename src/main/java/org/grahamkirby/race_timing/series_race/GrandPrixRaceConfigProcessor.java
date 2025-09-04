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
package org.grahamkirby.race_timing.series_race;

import org.grahamkirby.race_timing.common.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.grahamkirby.race_timing.common.Config.*;

@SuppressWarnings("preview")
public class GrandPrixRaceConfigProcessor implements ConfigProcessor {

    private Race race;

    public void setRace(Race race) {
        this.race = race;
    }

    private static final List<String> REQUIRED_STRING_PROPERTY_KEYS =
        List.of(KEY_YEAR, KEY_RACE_NAME_FOR_RESULTS, KEY_RACE_NAME_FOR_FILENAMES, KEY_RACES, KEY_RACE_TEMPORAL_ORDER, KEY_QUALIFYING_CLUBS);

    private static final List<String> REQUIRED_PATH_PROPERTY_KEYS =
        List.of(KEY_ENTRY_CATEGORIES_PATH, KEY_PRIZE_CATEGORIES_PATH, KEY_RACE_CATEGORIES_PATH);

    private static final List<String> OPTIONAL_PATH_WITH_DEFAULT_PROPERTY_KEYS =
        List.of(KEY_CAPITALISATION_STOP_WORDS_PATH, KEY_NORMALISED_HTML_ENTITIES_PATH, KEY_NORMALISED_CLUB_NAMES_PATH, KEY_GENDER_ELIGIBILITY_MAP_PATH);

    private static final List<Path> OPTIONAL_PATH_DEFAULT_PROPERTIES =
        List.of(DEFAULT_CAPITALISATION_STOP_WORDS_PATH, DEFAULT_NORMALISED_HTML_ENTITIES_PATH, DEFAULT_NORMALISED_CLUB_NAMES_PATH, DEFAULT_GENDER_ELIGIBILITY_MAP_PATH);

    @Override
    public Config loadConfig(final Path config_file_path) {

        try {
            final Properties properties = loadProperties(config_file_path);

            CommonConfigProcessor commonConfigProcessor = new CommonConfigProcessor(race, config_file_path, properties);
            final Map<String, Object> config_values = commonConfigProcessor.getConfigValues();

            commonConfigProcessor.addRequiredStringProperties(REQUIRED_STRING_PROPERTY_KEYS);

            commonConfigProcessor.addRequiredPathProperties(REQUIRED_PATH_PROPERTY_KEYS);
            commonConfigProcessor.addOptionalPathProperties(OPTIONAL_PATH_WITH_DEFAULT_PROPERTY_KEYS, OPTIONAL_PATH_DEFAULT_PROPERTIES);

            commonConfigProcessor.addRequiredProperty(KEY_SCORE_FOR_MEDIAN_POSITION, Integer::parseInt);
            commonConfigProcessor.addRequiredProperty(KEY_NUMBER_OF_RACES_IN_SERIES, Integer::parseInt);
            commonConfigProcessor.addRequiredProperty(KEY_MINIMUM_NUMBER_OF_RACES, Integer::parseInt);

            return new ConfigImpl(config_values, config_file_path);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

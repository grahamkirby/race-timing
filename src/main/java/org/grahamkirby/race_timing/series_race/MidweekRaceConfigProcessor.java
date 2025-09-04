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

@SuppressWarnings("preview")
public class MidweekRaceConfigProcessor implements ConfigProcessor {

    private Race race;

    public void setRace(Race race) {
        this.race = race;
    }

    private static final List<String> REQUIRED_STRING_PROPERTY_KEYS =
        List.of(Config.KEY_YEAR, Config.KEY_RACE_NAME_FOR_RESULTS, Config.KEY_RACE_NAME_FOR_FILENAMES, Config.KEY_RACES, Config.KEY_NUMBER_OF_RACES_IN_SERIES, Config.KEY_MINIMUM_NUMBER_OF_RACES, Config.KEY_SCORE_FOR_FIRST_PLACE);

    private static final List<String> REQUIRED_PATH_PROPERTY_KEYS =
        List.of(Config.KEY_ENTRY_CATEGORIES_PATH, Config.KEY_PRIZE_CATEGORIES_PATH);

    private static final List<String> OPTIONAL_PATH_WITH_DEFAULT_PROPERTY_KEYS =
        List.of(Config.KEY_CAPITALISATION_STOP_WORDS_PATH, Config.KEY_NORMALISED_HTML_ENTITIES_PATH, Config.KEY_NORMALISED_CLUB_NAMES_PATH, Config.KEY_GENDER_ELIGIBILITY_MAP_PATH);

    private static final List<Path> OPTIONAL_PATH_DEFAULT_PROPERTIES =
        List.of(Config.DEFAULT_CAPITALISATION_STOP_WORDS_PATH, Config.DEFAULT_NORMALISED_HTML_ENTITIES_PATH, Config.DEFAULT_NORMALISED_CLUB_NAMES_PATH, Config.DEFAULT_GENDER_ELIGIBILITY_MAP_PATH);

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public Config loadConfig(final Path config_file_path) {

        try {
            final Properties properties = Config.loadProperties(config_file_path);

            CommonConfigProcessor commonConfigProcessor = new CommonConfigProcessor(race, config_file_path, properties);
            final Map<String, Object> config_values = commonConfigProcessor.getConfigValues();

            commonConfigProcessor.addRequiredStringProperties(REQUIRED_STRING_PROPERTY_KEYS);
            commonConfigProcessor.addRequiredPathProperties(REQUIRED_PATH_PROPERTY_KEYS);
            commonConfigProcessor.addOptionalPathProperties(OPTIONAL_PATH_WITH_DEFAULT_PROPERTY_KEYS, OPTIONAL_PATH_DEFAULT_PROPERTIES);

            return new ConfigImpl(config_values, config_file_path);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

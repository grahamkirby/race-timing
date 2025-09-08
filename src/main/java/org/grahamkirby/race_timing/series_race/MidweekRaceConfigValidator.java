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

import org.grahamkirby.race_timing.common.Config;
import org.grahamkirby.race_timing.common.ConfigProcessor;
import org.grahamkirby.race_timing.common.Normalisation;
import org.grahamkirby.race_timing.common.Race;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.format.DateTimeParseException;

import static org.grahamkirby.race_timing.common.Config.*;

public class MidweekRaceConfigValidator implements ConfigProcessor {

    public void processConfig(final Race race) {

        var config = race.getConfig();

        validateKeyPresent(KEY_YEAR, race);
        validateKeyPresent(KEY_RACE_NAME_FOR_FILENAMES, race);
        validateKeyPresent(KEY_RACE_NAME_FOR_RESULTS, race);
        validateKeyPresent(KEY_ENTRY_CATEGORIES_PATH, race);
        validateKeyPresent(KEY_PRIZE_CATEGORIES_PATH, race);
        validateKeyPresent(KEY_RACES, race);
        validateKeyPresent(KEY_NUMBER_OF_RACES_IN_SERIES, race);
        validateKeyPresent(KEY_MINIMUM_NUMBER_OF_RACES, race);
        validateKeyPresent(KEY_SCORE_FOR_FIRST_PLACE, race);

        validateFileExists(KEY_ENTRY_CATEGORIES_PATH, race);
        validateFileExists(KEY_PRIZE_CATEGORIES_PATH, race);
    }

    public static void validateKeyPresent(final String key, final Race race) {

        if (!race.getConfig().containsKey(key))
            throw new RuntimeException(STR."no entry for key '\{key}' in file '\{race.config_file_path.getFileName()}'");
    }

    public static void validateFileExists(final String key, final Race race) {

        final Path path = (Path) race.getConfig().get(key);
        if (!Files.exists(path))
            throw new RuntimeException(STR."invalid entry '\{path.getFileName()}' for key '\{key}' in file '\{race.config_file_path.getFileName()}'");
    }
}

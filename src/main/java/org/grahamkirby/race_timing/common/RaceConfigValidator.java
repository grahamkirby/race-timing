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

import java.nio.file.Files;
import java.nio.file.Path;

import static org.grahamkirby.race_timing.common.Config.*;

public class RaceConfigValidator implements ConfigProcessor {

    public void processConfig(final Config config) {

        validateKeyPresent(KEY_YEAR, config);
        validateKeyPresent(KEY_RACE_NAME_FOR_FILENAMES, config);
        validateKeyPresent(KEY_RACE_NAME_FOR_RESULTS, config);
        validateKeyPresent(KEY_ENTRY_CATEGORIES_PATH, config);
        validateKeyPresent(KEY_PRIZE_CATEGORIES_PATH, config);

        validateFileExists(KEY_ENTRY_CATEGORIES_PATH, config);
        validateFileExists(KEY_PRIZE_CATEGORIES_PATH, config);
    }

    public static void validateKeyPresent(final String key, final Config config) {

        if (!config.containsKey(key))
            throw new RuntimeException("no entry for key '" + key + "' in file '" + config.getConfigPath().getFileName() + "'");
    }

    public static void validateFileExists(final String key, final Config config) {

        final Path path = config.getPathConfig(key);

        if (!Files.exists(path))
            throw new RuntimeException("invalid entry '" + path.getFileName() + "' for key '" + key + "' in file '" + config.getConfigPath().getFileName() + "'");
    }
}

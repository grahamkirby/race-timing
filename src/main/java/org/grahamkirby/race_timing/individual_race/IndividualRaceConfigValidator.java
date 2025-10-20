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
package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.common.ConfigProcessor;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.Race2;

import java.nio.file.Path;

import static org.grahamkirby.race_timing.common.Config.*;

public class IndividualRaceConfigValidator implements ConfigProcessor {

    public void processConfig(final Race2 race) {

        final var config = race.getConfig();

        if (!(config.containsKey(KEY_RAW_RESULTS_PATH) || config.containsKey(KEY_RESULTS_PATH)))
            throw new RuntimeException("no entry for either of keys '" + KEY_RAW_RESULTS_PATH + "' or '" + KEY_RESULTS_PATH + "' in file '" + race.getConfig().getConfigPath().getFileName() + "'");

        validateDNFRecords((String) config.get(KEY_DNF_FINISHERS), race.getConfig().getConfigPath());
    }

    public static void validateDNFRecords(final String dnf_string, final Path config_file_path) {

        if (dnf_string != null && !dnf_string.isBlank())
            for (final String individual_dnf_string : dnf_string.split(","))
                try {
                    // Each DNF string contains single bib number.
                    Integer.parseInt(individual_dnf_string);

                } catch (final NumberFormatException e) {
                    throw new RuntimeException("invalid entry '" + dnf_string +"' for key '" + KEY_DNF_FINISHERS + "' in file '" + config_file_path.getFileName() + "'", e);
                }
    }
}

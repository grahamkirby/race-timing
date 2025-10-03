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
package org.grahamkirby.race_timing.relay_race;

import org.grahamkirby.race_timing.common.ConfigProcessor;
import org.grahamkirby.race_timing.common.Normalisation;
import org.grahamkirby.race_timing.common.Race;

import java.nio.file.Path;
import java.time.Duration;
import java.time.format.DateTimeParseException;

import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.RaceConfigValidator.validateFileExists;
import static org.grahamkirby.race_timing.common.RaceConfigValidator.validateKeyPresent;

public class RelayRaceConfigValidator implements ConfigProcessor {

    public void processConfig(final Race race) {

        final var config = race.getConfig();

        validateKeyPresent(KEY_RAW_RESULTS_PATH, race);
        validateKeyPresent(KEY_ENTRIES_PATH, race);
        validateKeyPresent(KEY_NUMBER_OF_LEGS, race);
        validateKeyPresent(KEY_PAIRED_LEGS, race);

        validateFileExists(KEY_RAW_RESULTS_PATH, race);
        validateFileExists(KEY_ENTRIES_PATH, race);

        // Each DNF string contains single bib number.
        validateDNFRecords((String) config.get(KEY_DNF_FINISHERS), race.config_file_path);
        validateMassStartTimes((String) config.get(KEY_MASS_START_ELAPSED_TIMES), (int) config.get(KEY_NUMBER_OF_LEGS), race.config_file_path);
    }

    public static void validateDNFRecords(final String dnf_string, final Path config_file_path) {

        if (dnf_string != null && !dnf_string.isBlank())
            for (final String individual_dnf_string : dnf_string.split(",")) {
                try {
                    final String[] elements = individual_dnf_string.split("/");
                    Integer.parseInt(elements[0]);
                    Integer.parseInt(elements[1]);

                } catch (final NumberFormatException e) {
                    throw new RuntimeException("invalid entry '" + dnf_string + "' for key '" + KEY_DNF_FINISHERS + "' in file '" + config_file_path.getFileName() + "'", e);
                }
            }
    }

    private void validateMassStartTimes(final String mass_start_elapsed_times, final int number_of_legs, final Path config_file_path) {

        Duration previous_time = null;

        if (mass_start_elapsed_times != null)
            for (final String leg_time_string : mass_start_elapsed_times.split(",")) {

                final String[] split = leg_time_string.split("/");

                final Duration mass_start_time;
                try {
                    if (split.length < 2)
                        throw new RuntimeException("invalid mass start time for key '" + KEY_MASS_START_ELAPSED_TIMES + "' in file '" + config_file_path.getFileName() + "'");

                    final String time_string = split[1];
                    mass_start_time = Normalisation.parseTime(time_string);

                } catch (final DateTimeParseException _) {
                    throw new RuntimeException("invalid mass start time for key '" + KEY_MASS_START_ELAPSED_TIMES + "' in file '" + config_file_path.getFileName() + "'");
                }

                try {
                    final int leg_number = Integer.parseInt(split[0]);

                    if (leg_number < 1 || leg_number > number_of_legs)
                        throw new RuntimeException("invalid leg number for key '" + KEY_MASS_START_ELAPSED_TIMES + "' in file '" + config_file_path.getFileName() + "'");
                }
                catch (NumberFormatException _) {
                    throw new RuntimeException("invalid leg number for key '" + KEY_MASS_START_ELAPSED_TIMES + "' in file '" + config_file_path.getFileName() + "'");
                }

                if (previous_time != null && previous_time.compareTo(mass_start_time) > 0)
                    throw new RuntimeException("invalid mass start time order for key '" + KEY_MASS_START_ELAPSED_TIMES + "' in file '" + config_file_path.getFileName() + "'");

                previous_time = mass_start_time;
            }
    }
}

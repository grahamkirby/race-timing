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
package org.grahamkirby.race_timing_experimental.relay_race;

import org.grahamkirby.race_timing.relay_race.RelayRace;
import org.grahamkirby.race_timing_experimental.common.*;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.grahamkirby.race_timing.common.Normalisation.parseTime;
import static org.grahamkirby.race_timing.common.Race.loadProperties;
import static org.grahamkirby.race_timing.single_race.SingleRace.KEY_DNF_FINISHERS;
import static org.grahamkirby.race_timing_experimental.common.Config.*;

@SuppressWarnings("preview")
public class RelayRaceConfigProcessor implements ConfigProcessor {

    /** Default entry map with 4 elements (bib number, full name, club, category), and no column combining or re-ordering. */
    private static final String DEFAULT_ENTRY_COLUMN_MAP = "1,2,3,4";

    private Race race;

    public void setRace(Race race) {
        this.race = race;
    }

    public String dnf_string;

    private static final List<String> REQUIRED_STRING_PROPERTY_KEYS =
        List.of(KEY_YEAR, KEY_RACE_NAME_FOR_RESULTS, KEY_RACE_NAME_FOR_FILENAMES, KEY_PAIRED_LEGS);

    private static final List<String> OPTIONAL_STRING_PROPERTY_KEYS =
        List.of(KEY_DNF_FINISHERS, KEY_RESULTS_PATH, KEY_INDIVIDUAL_EARLY_STARTS, KEY_ENTRY_COLUMN_MAP, KEY_INDIVIDUAL_LEG_STARTS, KEY_MASS_START_ELAPSED_TIMES);

    private static final List<String> REQUIRED_PATH_PROPERTY_KEYS =
        List.of(KEY_ENTRY_CATEGORIES_PATH, KEY_PRIZE_CATEGORIES_PATH, KEY_ENTRIES_PATH, KEY_RAW_RESULTS_PATH);

    private static final List<String> OPTIONAL_PATH_PROPERTY_KEYS =
        List.of(KEY_CATEGORY_MAP_PATH, KEY_PAPER_RESULTS_PATH, KEY_ANNOTATIONS_PATH);

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

            commonConfigProcessor.addRequiredProperty(KEY_NUMBER_OF_LEGS, Integer::parseInt);
            commonConfigProcessor.addOptionalProperty(KEY_START_OFFSET, Normalisation::parseTime, _ -> Duration.ZERO);

            final Map<String, Object> config_values = commonConfigProcessor.getConfigValues();

            validateDNFRecords(config_values, config_file_path);
            validateMassStartTimes((String) config_values.get(KEY_MASS_START_ELAPSED_TIMES), config_file_path);

            return new ConfigImpl(config_values);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateDNFRecords(Map<String, Object> config_values, Path config_file_path) {

        final String dnf_string = (String) config_values.get(KEY_DNF_FINISHERS);

        if (dnf_string != null && !dnf_string.isBlank())
            for (final String individual_dnf_string : dnf_string.split(","))
                try {
                    // String of form "bib-number/leg-number"

                    final String[] elements = individual_dnf_string.split("/");
                    Integer.parseInt(elements[0]);
                    Integer.parseInt(elements[1]);

                } catch (final NumberFormatException _) {
                    throw new RuntimeException(STR."invalid entry '\{dnf_string}' for key '\{KEY_DNF_FINISHERS}' in file '\{config_file_path.getFileName()}'");
                }
    }

    private void validateMassStartTimes(final String mass_start_elapsed_times, Path config_file_path) {

        if (mass_start_elapsed_times != null) {

            Duration previous_time = null;
            for (final String time_string : mass_start_elapsed_times.split(",")) {

                final Duration mass_start_time;
                try {
                    mass_start_time = parseTime(time_string);
                } catch (final DateTimeParseException _) {
                    throw new RuntimeException(STR."invalid mass start time for key '\{RelayRace.KEY_MASS_START_ELAPSED_TIMES}' in file '\{config_file_path.getFileName()}'");
                }

                if (previous_time != null && previous_time.compareTo(mass_start_time) > 0)
                    throw new RuntimeException(STR."invalid mass start time order for key '\{RelayRace.KEY_MASS_START_ELAPSED_TIMES}' in file '\{config_file_path.getFileName()}'");

                previous_time = mass_start_time;
            }
        }
    }
}

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
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.grahamkirby.race_timing.common.Race.loadProperties;
import static org.grahamkirby.race_timing_experimental.common.CommonConfigProcessor.makeDefaultEntryColumnMap;
import static org.grahamkirby.race_timing_experimental.common.CommonConfigProcessor.validateDNFRecords;
import static org.grahamkirby.race_timing_experimental.common.Config.*;
import static org.grahamkirby.race_timing_experimental.common.Normalisation.format;
import static org.grahamkirby.race_timing_experimental.common.Normalisation.parseTime;

@SuppressWarnings("preview")
public class RelayRaceConfigProcessor implements ConfigProcessor {

//    private static final String DEFAULT_ENTRY_COLUMN_MAP = "1,2,3,4";

    private Race race;

    public void setRace(Race race) {
        this.race = race;
    }

    public String dnf_string;

    private static final List<String> REQUIRED_STRING_PROPERTY_KEYS =
        List.of(KEY_YEAR, KEY_RACE_NAME_FOR_RESULTS, KEY_RACE_NAME_FOR_FILENAMES, KEY_PAIRED_LEGS);

    private static final List<String> OPTIONAL_STRING_PROPERTY_KEYS =
        List.of(KEY_DNF_FINISHERS, KEY_RESULTS_PATH, KEY_INDIVIDUAL_EARLY_STARTS, KEY_ENTRY_COLUMN_MAP, KEY_INDIVIDUAL_LEG_STARTS);

//    private static final List<String> OPTIONAL_STRING_WITH_DEFAULT_PROPERTY_KEYS =
//        List.of(KEY_ENTRY_COLUMN_MAP);
//
//    private static final List<String> OPTIONAL_STRING_DEFAULT_PROPERTIES =
//        List.of(DEFAULT_ENTRY_COLUMN_MAP);

    private static final List<String> REQUIRED_PATH_PROPERTY_KEYS =
        List.of(KEY_ENTRY_CATEGORIES_PATH, KEY_PRIZE_CATEGORIES_PATH, KEY_ENTRIES_PATH, KEY_RAW_RESULTS_PATH);

    private static final List<String> OPTIONAL_PATH_PROPERTY_KEYS =
        List.of(KEY_CATEGORY_MAP_PATH, KEY_PAPER_RESULTS_PATH, KEY_ANNOTATIONS_PATH);

    private static final List<String> OPTIONAL_PATH_WITH_DEFAULT_PROPERTY_KEYS =
        List.of(KEY_CAPITALISATION_STOP_WORDS_PATH, KEY_NORMALISED_HTML_ENTITIES_PATH, KEY_NORMALISED_CLUB_NAMES_PATH, KEY_GENDER_ELIGIBILITY_MAP_PATH);

    private static final List<Path> OPTIONAL_PATH_DEFAULT_PROPERTIES =
        List.of(DEFAULT_CAPITALISATION_STOP_WORDS_PATH, DEFAULT_NORMALISED_HTML_ENTITIES_PATH, DEFAULT_NORMALISED_CLUB_NAMES_PATH, DEFAULT_GENDER_ELIGIBILITY_MAP_PATH);

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public Config loadConfig(final Path config_file_path) {

        try {
            final Properties properties = loadProperties(config_file_path);

            CommonConfigProcessor commonConfigProcessor = new CommonConfigProcessor(race, config_file_path, properties);
            final Map<String, Object> config_values = commonConfigProcessor.getConfigValues();

            commonConfigProcessor.addRequiredStringProperties(REQUIRED_STRING_PROPERTY_KEYS);
            commonConfigProcessor.addOptionalStringProperties(OPTIONAL_STRING_PROPERTY_KEYS);

            commonConfigProcessor.addRequiredPathProperties(REQUIRED_PATH_PROPERTY_KEYS);
            commonConfigProcessor.addOptionalPathProperties(OPTIONAL_PATH_PROPERTY_KEYS);
            commonConfigProcessor.addOptionalPathProperties(OPTIONAL_PATH_WITH_DEFAULT_PROPERTY_KEYS, OPTIONAL_PATH_DEFAULT_PROPERTIES);

            commonConfigProcessor.addRequiredProperty(KEY_NUMBER_OF_LEGS, Integer::parseInt);
            final int number_of_legs = (int) config_values.get(KEY_NUMBER_OF_LEGS);

            commonConfigProcessor.addOptionalProperty(KEY_START_OFFSET, Normalisation::parseTime, _ -> Duration.ZERO);

            commonConfigProcessor.addOptionalProperty(KEY_MASS_START_ELAPSED_TIMES, s -> s, _ -> defaultMassStartElapsedTimes(number_of_legs));

            // Default entry map with elements (bib number, team name, category, plus one per leg), and no column combining or re-ordering.
            commonConfigProcessor.addOptionalProperty(KEY_ENTRY_COLUMN_MAP, s -> s, _ -> makeDefaultEntryColumnMap(number_of_legs + 3));

            // Each DNF string contains single bib number.
            Consumer<String> dnf_string_checker = individual_dnf_string -> {

                final String[] elements = individual_dnf_string.split("/");
                Integer.parseInt(elements[0]);
                Integer.parseInt(elements[1]);
            };
            validateDNFRecords((String) config_values.get(KEY_DNF_FINISHERS), dnf_string_checker, config_file_path);

            validateMassStartTimes((String) config_values.get(KEY_MASS_START_ELAPSED_TIMES), config_file_path);

            return new ConfigImpl(config_values);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String defaultMassStartElapsedTimes(final int number_of_legs) {

        return Stream.generate(() -> format(Duration.ZERO)).
            limit(number_of_legs).
            collect(Collectors.joining(","));
    }

    private void validateMassStartTimes(final String mass_start_elapsed_times, final Path config_file_path) {

        Duration previous_time = null;

        for (final String time_string : mass_start_elapsed_times.split(",")) {

            final Duration mass_start_time;
            try {
                mass_start_time = parseTime(time_string);

            } catch (final DateTimeParseException _) {
                throw new RuntimeException(STR."invalid mass start time for key '\{KEY_MASS_START_ELAPSED_TIMES}' in file '\{config_file_path.getFileName()}'");
            }

            if (previous_time != null && previous_time.compareTo(mass_start_time) > 0)
                throw new RuntimeException(STR."invalid mass start time order for key '\{KEY_MASS_START_ELAPSED_TIMES}' in file '\{config_file_path.getFileName()}'");

            previous_time = mass_start_time;
        }
    }
}

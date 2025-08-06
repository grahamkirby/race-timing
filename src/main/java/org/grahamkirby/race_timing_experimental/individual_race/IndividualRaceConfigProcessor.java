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
package org.grahamkirby.race_timing_experimental.individual_race;

import org.grahamkirby.race_timing_experimental.common.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.grahamkirby.race_timing.common.Race.loadProperties;
import static org.grahamkirby.race_timing.single_race.SingleRace.KEY_DNF_FINISHERS;
import static org.grahamkirby.race_timing_experimental.common.Config.*;

@SuppressWarnings("preview")
public class IndividualRaceConfigProcessor implements ConfigProcessor {

    /** Default entry map with 4 elements (bib number, full name, club, category), and no column combining or re-ordering. */
    private static final String DEFAULT_ENTRY_COLUMN_MAP = "1,2,3,4";

    private Race race;

    public void setRace(Race race) {
        this.race = race;
    }

    public String dnf_string;

    private static final List<String> REQUIRED_STRING_PROPERTY_KEYS =
        List.of(KEY_YEAR, KEY_RACE_NAME_FOR_RESULTS, KEY_RACE_NAME_FOR_FILENAMES);

    private static final List<String> OPTIONAL_STRING_PROPERTY_KEYS =
        List.of(KEY_DNF_FINISHERS, KEY_RESULTS_PATH, KEY_INDIVIDUAL_EARLY_STARTS, KEY_MEDIAN_TIME);

    private static final List<String> REQUIRED_PATH_PROPERTY_KEYS =
        List.of(KEY_ENTRY_CATEGORIES_PATH, KEY_PRIZE_CATEGORIES_PATH, KEY_ENTRIES_PATH, KEY_RAW_RESULTS_PATH);

    private static final List<String> OPTIONAL_PATH_PROPERTY_KEYS =
        List.of(KEY_CATEGORY_MAP_PATH);


    @Override
    public Config loadConfig(final Path config_file_path) {

        try {
            final Properties properties = loadProperties(config_file_path);

            CommonConfigProcessor commonConfigProcessor = new CommonConfigProcessor(race, config_file_path, properties);
            final Map<String, Object> config_values = commonConfigProcessor.getConfigValues();

            commonConfigProcessor.addRequiredStringProperties(REQUIRED_STRING_PROPERTY_KEYS);
            commonConfigProcessor.addRequiredPathProperties(REQUIRED_PATH_PROPERTY_KEYS);
            commonConfigProcessor.addOptionalStringProperties(OPTIONAL_STRING_PROPERTY_KEYS);
            commonConfigProcessor.addOptionalPathProperties(OPTIONAL_PATH_PROPERTY_KEYS);

            if (properties.getProperty(KEY_CAPITALISATION_STOP_WORDS_PATH) != null)
                config_values.put(KEY_CAPITALISATION_STOP_WORDS_PATH, race.interpretPath(Path.of(properties.getProperty(KEY_CAPITALISATION_STOP_WORDS_PATH))));
            else
                config_values.put(KEY_CAPITALISATION_STOP_WORDS_PATH, race.interpretPath(DEFAULT_CAPITALISATION_STOP_WORDS_PATH));

            if (properties.getProperty(KEY_NORMALISED_HTML_ENTITIES_PATH) != null)
                config_values.put(KEY_NORMALISED_HTML_ENTITIES_PATH, race.interpretPath(Path.of(properties.getProperty(KEY_NORMALISED_HTML_ENTITIES_PATH))));
            else
                config_values.put(KEY_NORMALISED_HTML_ENTITIES_PATH, race.interpretPath(DEFAULT_NORMALISED_HTML_ENTITIES_PATH));

            if (properties.getProperty(KEY_NORMALISED_CLUB_NAMES_PATH) != null)
                config_values.put(KEY_NORMALISED_CLUB_NAMES_PATH, race.interpretPath(Path.of(properties.getProperty(KEY_NORMALISED_CLUB_NAMES_PATH))));
            else
                config_values.put(KEY_NORMALISED_CLUB_NAMES_PATH, race.interpretPath(DEFAULT_NORMALISED_CLUB_NAMES_PATH));

            if (properties.getProperty(KEY_GENDER_ELIGIBILITY_MAP_PATH) != null)
                config_values.put(KEY_GENDER_ELIGIBILITY_MAP_PATH, race.interpretPath(Path.of(properties.getProperty(KEY_GENDER_ELIGIBILITY_MAP_PATH))));
            else
                config_values.put(KEY_GENDER_ELIGIBILITY_MAP_PATH, race.interpretPath(DEFAULT_GENDER_ELIGIBILITY_MAP_PATH));

            if (properties.getProperty(KEY_ENTRY_COLUMN_MAP) != null)
                config_values.put(KEY_ENTRY_COLUMN_MAP, properties.getProperty(KEY_ENTRY_COLUMN_MAP));
            else
                config_values.put(KEY_ENTRY_COLUMN_MAP, DEFAULT_ENTRY_COLUMN_MAP);

            validateDNFRecords(config_values, config_file_path);

            return new ConfigImpl(config_values);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateDNFRecords(Map<String, Object> config_values, Path config_file_path) {

        final String dnf_string = (String) config_values.get(KEY_DNF_FINISHERS);

        if (dnf_string != null && !dnf_string.isBlank())
            for (final String bib_number : dnf_string.split(",")) {
                try {
                    Integer.parseInt(bib_number);

                } catch (final NumberFormatException e) {
                    throw new RuntimeException(STR."invalid entry '\{bib_number}' for key '\{KEY_DNF_FINISHERS}' in file '\{config_file_path.getFileName()}'", e);
                }
            }
    }
}

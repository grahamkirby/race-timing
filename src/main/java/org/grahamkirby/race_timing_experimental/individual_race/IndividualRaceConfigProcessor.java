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

import org.grahamkirby.race_timing_experimental.common.Config;
import org.grahamkirby.race_timing_experimental.common.ConfigImpl;
import org.grahamkirby.race_timing_experimental.common.ConfigProcessor;
import org.grahamkirby.race_timing_experimental.common.Race;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.grahamkirby.race_timing.common.Race.loadProperties;
import static org.grahamkirby.race_timing.single_race.SingleRace.KEY_DNF_FINISHERS;
import static org.grahamkirby.race_timing_experimental.common.Config.*;

@SuppressWarnings("preview")
public class IndividualRaceConfigProcessor implements ConfigProcessor {

    private static final Path DEFAULT_CAPITALISATION_STOP_WORDS_PATH = Path.of(STR."\{DEFAULT_CONFIG_ROOT_PATH}/capitalisation_stop_words\{SUFFIX_CSV}");

    private static final Path DEFAULT_NORMALISED_HTML_ENTITIES_PATH = Path.of(STR."\{DEFAULT_CONFIG_ROOT_PATH}/html_entities\{SUFFIX_CSV}");
    private static final Path DEFAULT_NORMALISED_CLUB_NAMES_PATH = Path.of(STR."\{DEFAULT_CONFIG_ROOT_PATH}/club_names\{SUFFIX_CSV}");
    private static final Path DEFAULT_GENDER_ELIGIBILITY_MAP_PATH = Path.of(STR."\{DEFAULT_CONFIG_ROOT_PATH}/gender_eligibility_default\{SUFFIX_CSV}");

    /** Default entry map with 4 elements (bib number, full name, club, category), and no column combining or re-ordering. */
    private static final String DEFAULT_ENTRY_COLUMN_MAP = "1,2,3,4";

    private Race race;

    public void setRace(Race race) {
        this.race = race;
    }

    public String dnf_string;

    @Override
    public Config loadConfig(Path config_file_path) {

        try {
            final Map<String, Object> config_values = new HashMap<>();

            final Properties properties = loadProperties(config_file_path);

            String year = properties.getProperty(KEY_YEAR);
            if (year == null)
                throw new RuntimeException(STR."no entry for key '\{KEY_YEAR}' in file '\{config_file_path.getFileName()}'");
            config_values.put(KEY_YEAR, year);

            String race_name_for_results = properties.getProperty(KEY_RACE_NAME_FOR_RESULTS);
            if (race_name_for_results == null)
                throw new RuntimeException(STR."no entry for key '\{KEY_RACE_NAME_FOR_RESULTS}' in file '\{config_file_path.getFileName()}'");
            config_values.put(KEY_RACE_NAME_FOR_RESULTS, race_name_for_results);

            String race_name_for_filenames = properties.getProperty(KEY_RACE_NAME_FOR_FILENAMES);
            if (race_name_for_filenames == null)
                throw new RuntimeException(STR."no entry for key '\{KEY_RACE_NAME_FOR_FILENAMES}' in file '\{config_file_path.getFileName()}'");
            config_values.put(KEY_RACE_NAME_FOR_FILENAMES, race_name_for_filenames);

            String entry_categories_path = properties.getProperty(KEY_ENTRY_CATEGORIES_PATH);
            if (entry_categories_path == null)
                throw new RuntimeException(STR."no entry for key '\{KEY_ENTRY_CATEGORIES_PATH}' in file '\{config_file_path.getFileName()}'");
            config_values.put(KEY_ENTRY_CATEGORIES_PATH, race.interpretPath(Path.of(entry_categories_path)));









            String prize_categories_path = properties.getProperty(KEY_PRIZE_CATEGORIES_PATH);
            if (prize_categories_path == null)
                throw new RuntimeException(STR."no entry for key '\{KEY_PRIZE_CATEGORIES_PATH}' in file '\{config_file_path.getFileName()}'");
            config_values.put(KEY_PRIZE_CATEGORIES_PATH, race.interpretPath(Path.of(prize_categories_path)));




            if (properties.getProperty(KEY_DNF_FINISHERS) != null)
                config_values.put(KEY_DNF_FINISHERS, properties.getProperty(KEY_DNF_FINISHERS));

            String entries_path = properties.getProperty(KEY_ENTRIES_PATH);
            if (entries_path == null)
                throw new RuntimeException(STR."no entry for key '\{KEY_ENTRIES_PATH}' in file '\{config_file_path.getFileName()}'");
            config_values.put(KEY_ENTRIES_PATH, race.interpretPath(Path.of(entries_path)));
            String raw_results_path = properties.getProperty(KEY_RAW_RESULTS_PATH);
            if (raw_results_path == null)
                throw new RuntimeException(STR."no entry for key '\{KEY_RAW_RESULTS_PATH}' in file '\{config_file_path.getFileName()}'");
            config_values.put(KEY_RAW_RESULTS_PATH, race.interpretPath(Path.of(raw_results_path)));

            if (properties.getProperty(KEY_RESULTS_PATH) != null)
                config_values.put(KEY_RESULTS_PATH, properties.getProperty(KEY_RESULTS_PATH));

            config_values.put(KEY_INDIVIDUAL_EARLY_STARTS, properties.getProperty(KEY_INDIVIDUAL_EARLY_STARTS));

            if (properties.getProperty(KEY_DNF_FINISHERS) != null)
                config_values.put(KEY_MEDIAN_TIME, properties.getProperty(KEY_MEDIAN_TIME));

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

            final String category_map_path = properties.getProperty(KEY_CATEGORY_MAP_PATH);
            if (category_map_path != null) config_values.put(KEY_CATEGORY_MAP_PATH, race.interpretPath(Path.of(category_map_path)));

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

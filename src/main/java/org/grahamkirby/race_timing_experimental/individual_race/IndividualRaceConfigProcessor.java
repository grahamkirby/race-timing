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
import org.grahamkirby.race_timing_experimental.common.ConfigProcessor;
import org.grahamkirby.race_timing_experimental.common.Race;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.grahamkirby.race_timing.common.Race.loadProperties;

public class IndividualRaceConfigProcessor implements ConfigProcessor {

    // Configuration file keys.
    public static final String KEY_YEAR = "YEAR";
    public static final String KEY_RACE_NAME_FOR_RESULTS = "RACE_NAME_FOR_RESULTS";
    public static final String KEY_RACE_NAME_FOR_FILENAMES = "RACE_NAME_FOR_FILENAMES";
    public static final String KEY_CATEGORIES_ENTRY_PATH = "CATEGORIES_ENTRY_PATH";
    public static final String KEY_CATEGORIES_PRIZE_PATH = "CATEGORIES_PRIZE_PATH";
    public static final String KEY_RESULTS_PATH = "RESULTS_PATH";
    public static final String KEY_DNF_FINISHERS = "DNF_FINISHERS";
    private static final String KEY_MEDIAN_TIME = "MEDIAN_TIME";
    public static final String KEY_ENTRIES_PATH = "ENTRIES_PATH";
    public static final String KEY_RAW_RESULTS_PATH = "RAW_RESULTS_PATH";
    private static final String KEY_INDIVIDUAL_EARLY_STARTS = "INDIVIDUAL_EARLY_STARTS";

    private Race race;

    public void setRace(Race race) {
        this.race = race;
    }

    public String dnf_string;

    @Override
    public Config loadConfig(Path config_file_path) {

        try {
            Map<String, Object> config_values = new HashMap<>();

            Properties properties = loadProperties(config_file_path);

            config_values.put(KEY_YEAR, properties.getProperty(KEY_YEAR));
            config_values.put(KEY_RACE_NAME_FOR_RESULTS, properties.getProperty(KEY_RACE_NAME_FOR_RESULTS));
            config_values.put(KEY_RACE_NAME_FOR_FILENAMES, properties.getProperty(KEY_RACE_NAME_FOR_FILENAMES));
            config_values.put(KEY_CATEGORIES_ENTRY_PATH, properties.getProperty(KEY_CATEGORIES_ENTRY_PATH));
            config_values.put(KEY_CATEGORIES_PRIZE_PATH, properties.getProperty(KEY_CATEGORIES_PRIZE_PATH));

            if (properties.getProperty(KEY_DNF_FINISHERS) != null)
                config_values.put(KEY_DNF_FINISHERS, properties.getProperty(KEY_DNF_FINISHERS));
            config_values.put(KEY_ENTRIES_PATH, properties.getProperty(KEY_ENTRIES_PATH));
            config_values.put(KEY_RAW_RESULTS_PATH, properties.getProperty(KEY_RAW_RESULTS_PATH));
            config_values.put(KEY_RESULTS_PATH, properties.getProperty(KEY_RESULTS_PATH));
            config_values.put(KEY_INDIVIDUAL_EARLY_STARTS, properties.getProperty(KEY_INDIVIDUAL_EARLY_STARTS));
            if (properties.getProperty(KEY_DNF_FINISHERS) != null)
                config_values.put(KEY_MEDIAN_TIME, properties.getProperty(KEY_MEDIAN_TIME));



            return new ConfigImpl(config_values);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

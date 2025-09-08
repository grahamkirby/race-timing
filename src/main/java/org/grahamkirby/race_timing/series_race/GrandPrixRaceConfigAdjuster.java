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
import org.grahamkirby.race_timing.common.Race;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.grahamkirby.race_timing.common.Config.*;

@SuppressWarnings("preview")
public class GrandPrixRaceConfigAdjuster implements ConfigProcessor {

    public static String DEFAULT_CAPITALISATION_STOP_WORDS_PATH = STR."\{DEFAULT_CONFIG_ROOT_PATH}/capitalisation_stop_words.\{CSV_FILE_SUFFIX}";
    public static String DEFAULT_NORMALISED_HTML_ENTITIES_PATH = STR."\{DEFAULT_CONFIG_ROOT_PATH}/html_entities.\{CSV_FILE_SUFFIX}";
    public static String DEFAULT_NORMALISED_CLUB_NAMES_PATH = STR."\{DEFAULT_CONFIG_ROOT_PATH}/club_names.\{CSV_FILE_SUFFIX}";
    public static String DEFAULT_GENDER_ELIGIBILITY_MAP_PATH = STR."\{DEFAULT_CONFIG_ROOT_PATH}/gender_eligibility_default.\{CSV_FILE_SUFFIX}";

    private static final List<String> PATH_PROPERTY_KEYS =
        List.of(KEY_ENTRY_CATEGORIES_PATH, KEY_PRIZE_CATEGORIES_PATH, KEY_CAPITALISATION_STOP_WORDS_PATH,
            KEY_NORMALISED_HTML_ENTITIES_PATH, KEY_NORMALISED_CLUB_NAMES_PATH, KEY_GENDER_ELIGIBILITY_MAP_PATH,
            KEY_CATEGORY_MAP_PATH, KEY_RACE_CATEGORIES_PATH);

    @Override
    public void processConfig(Race race) {

        Config config = race.getConfig();

        config.addIfAbsent(KEY_CAPITALISATION_STOP_WORDS_PATH, DEFAULT_CAPITALISATION_STOP_WORDS_PATH);
        config.addIfAbsent(KEY_NORMALISED_HTML_ENTITIES_PATH, DEFAULT_NORMALISED_HTML_ENTITIES_PATH);
        config.addIfAbsent(KEY_NORMALISED_CLUB_NAMES_PATH, DEFAULT_NORMALISED_CLUB_NAMES_PATH);
        config.addIfAbsent(KEY_GENDER_ELIGIBILITY_MAP_PATH, DEFAULT_GENDER_ELIGIBILITY_MAP_PATH);

        for (String key : PATH_PROPERTY_KEYS)
            config.replaceIfPresent(key, s -> race.interpretPath(Path.of(s)));

        // Default entry map with elements (bib number, team name, category, plus one per leg), and no column combining or re-ordering.
        config.addIfAbsent(KEY_ENTRY_COLUMN_MAP, makeDefaultEntryColumnMap(4));

        config.replaceIfPresent(KEY_NUMBER_OF_RACES_IN_SERIES, Integer::parseInt);
        config.replaceIfPresent(KEY_MINIMUM_NUMBER_OF_RACES, Integer::parseInt);
        config.replaceIfPresent(KEY_SCORE_FOR_MEDIAN_POSITION, Integer::parseInt);
    }

    private static String makeDefaultEntryColumnMap(int numberOfColumns) {

        return Stream.iterate(1, i -> i + 1).
            map(Object::toString).
            limit(numberOfColumns).
            collect(Collectors.joining(","));
    }

}

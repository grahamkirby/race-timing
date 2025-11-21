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

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.grahamkirby.race_timing.common.Config.*;

@SuppressWarnings("preview")
public class RaceConfigAdjuster extends ConfigProcessor {

    public static String DEFAULT_CAPITALISATION_STOP_WORDS_PATH = DEFAULT_CONFIG_ROOT_PATH + "/capitalisation_stop_words." + CSV_FILE_SUFFIX;
    public static String DEFAULT_NORMALISED_HTML_ENTITIES_PATH = DEFAULT_CONFIG_ROOT_PATH + "/html_entities." + CSV_FILE_SUFFIX;
    public static String DEFAULT_NORMALISED_CLUB_NAMES_PATH = DEFAULT_CONFIG_ROOT_PATH + "/club_names." + CSV_FILE_SUFFIX;

    public RaceConfigAdjuster(final Config config) {

        super(config);
    }

    private static final List<String> PATH_PROPERTY_KEYS =
        List.of(
            KEY_CAPITALISATION_STOP_WORDS_PATH,
            KEY_CATEGORY_MAP_PATH,
            KEY_ENTRIES_PATH,
            KEY_ENTRY_CATEGORIES_PATH,
            KEY_NORMALISED_CLUB_NAMES_PATH,
            KEY_NORMALISED_HTML_ENTITIES_PATH,
            KEY_PRIZE_CATEGORIES_PATH,
            KEY_RAW_RESULTS_PATH,
            KEY_RESULTS_PATH
        );

    @Override
    public void processConfig() {

        config.addIfAbsent(KEY_CAPITALISATION_STOP_WORDS_PATH, DEFAULT_CAPITALISATION_STOP_WORDS_PATH);
        config.addIfAbsent(KEY_NORMALISED_CLUB_NAMES_PATH, DEFAULT_NORMALISED_CLUB_NAMES_PATH);
        config.addIfAbsent(KEY_NORMALISED_HTML_ENTITIES_PATH, DEFAULT_NORMALISED_HTML_ENTITIES_PATH);

        config.replaceIfPresent(PATH_PROPERTY_KEYS, s -> config.interpretPath(Path.of(s)));
        config.replaceIfPresent(KEY_PREFER_LOWER_PRIZE_IN_MORE_GENERAL_CATEGORY, Boolean::parseBoolean);

        config.addIfAbsent(KEY_PREFER_LOWER_PRIZE_IN_MORE_GENERAL_CATEGORY, true);
    }

    public static String makeDefaultEntryColumnMap(final int number_of_columns) {

        return Stream.iterate(1, i -> i + 1).
            map(Object::toString).
            limit(number_of_columns).
            collect(Collectors.joining(","));
    }
}

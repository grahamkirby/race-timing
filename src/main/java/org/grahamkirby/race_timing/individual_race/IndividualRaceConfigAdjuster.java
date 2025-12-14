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

import org.grahamkirby.race_timing.common.*;

import java.nio.file.Path;

import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.RaceConfigAdjuster.DEFAULT_CONFIG_ROOT_PATH;
import static org.grahamkirby.race_timing.common.RaceConfigAdjuster.makeDefaultEntryColumnMap;

@SuppressWarnings("preview")
public class IndividualRaceConfigAdjuster extends ConfigProcessor {

    public static final int DEFAULT_NUMBER_OF_COLUMNS = 4;

    public static String DEFAULT_ENTRY_CATEGORIES_PATH = DEFAULT_CONFIG_ROOT_PATH + "/categories_entry_individual_senior." + CSV_FILE_SUFFIX;
    public static String DEFAULT_PRIZE_CATEGORIES_PATH = DEFAULT_CONFIG_ROOT_PATH + "/categories_prize_individual_senior." + CSV_FILE_SUFFIX;

    public IndividualRaceConfigAdjuster(final Config config) {

        super(config);
    }

    @Override
    public void processConfig() {

        // Default entry map with 4 elements (bib number, full club, club, category), and no column combining or re-ordering.
        config.addIfAbsent(KEY_ENTRY_COLUMN_MAP, makeDefaultEntryColumnMap(DEFAULT_NUMBER_OF_COLUMNS));

        config.addIfAbsent(KEY_ENTRY_CATEGORIES_PATH, config.interpretPath(Path.of(DEFAULT_ENTRY_CATEGORIES_PATH)));
        config.addIfAbsent(KEY_PRIZE_CATEGORIES_PATH, config.interpretPath(Path.of(DEFAULT_PRIZE_CATEGORIES_PATH)));

        config.replaceIfPresent(KEY_TEAM_PRIZE_NUMBER_TO_COUNT, Integer::parseInt);
        config.replaceIfPresent(KEY_TIME_TRIAL_RUNNERS_PER_WAVE, Integer::parseInt);
        config.replaceIfPresent(KEY_TIME_TRIAL_INTER_WAVE_INTERVAL, NormalisationProcessor::parseTime);
    }
}

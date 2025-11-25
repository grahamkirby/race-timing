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

import java.util.List;

import static org.grahamkirby.race_timing.common.Config.*;

public class SeriesRaceConfigValidator extends ConfigProcessor {

    public SeriesRaceConfigValidator(final Config config) {

        super(config);
    }

    public void processConfig() {

        checkAllPresent(List.of(
            KEY_MINIMUM_NUMBER_OF_RACES,
            KEY_NUMBER_OF_RACES_IN_SERIES,
            KEY_RACES));

        checkNonePresent(List.of(
            KEY_INDIVIDUAL_LEG_STARTS,
            KEY_MASS_START_TIMES,
            KEY_MEDIAN_TIME,
            KEY_NUMBER_OF_LEGS,
            KEY_TEAM_PRIZE_NUMBER_TO_COUNT,
            KEY_PAIRED_LEGS,
            KEY_PAPER_RESULTS_PATH,
            KEY_RESULTS_PATH,
            KEY_TIME_TRIAL_INTER_WAVE_INTERVAL,
            KEY_TIME_TRIAL_RUNNERS_PER_WAVE));
    }
}

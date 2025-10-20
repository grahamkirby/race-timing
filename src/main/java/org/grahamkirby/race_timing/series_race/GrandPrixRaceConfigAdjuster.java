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
import org.grahamkirby.race_timing.common.Race2;

import java.nio.file.Path;

import static org.grahamkirby.race_timing.common.Config.KEY_RACE_CATEGORIES_PATH;
import static org.grahamkirby.race_timing.common.Config.KEY_SCORE_FOR_MEDIAN_POSITION;

@SuppressWarnings("preview")
public class GrandPrixRaceConfigAdjuster implements ConfigProcessor {

    @Override
    public void processConfig(Race2 race) {

        final Config config = race.getConfig();

        config.replaceIfPresent(KEY_RACE_CATEGORIES_PATH, s -> race.interpretPath(Path.of(s)));
        config.replaceIfPresent(KEY_SCORE_FOR_MEDIAN_POSITION, Integer::parseInt);
    }
}

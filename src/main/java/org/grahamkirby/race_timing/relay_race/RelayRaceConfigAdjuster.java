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
package org.grahamkirby.race_timing.relay_race;

import org.grahamkirby.race_timing.common.*;

import java.nio.file.Path;
import java.util.List;

import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.RaceConfigAdjuster.makeDefaultEntryColumnMap;

@SuppressWarnings("preview")
public class RelayRaceConfigAdjuster extends ConfigProcessor {

    private static final List<String> PATH_PROPERTY_KEYS =
        List.of(KEY_PAPER_RESULTS_PATH, KEY_ANNOTATIONS_PATH);

    public RelayRaceConfigAdjuster(final Config config) {

        super(config);
    }

    @Override
    public void processConfig() {

        config.addIfAbsent(KEY_RACE_START_TIME, "00:00:00");

        config.replaceIfPresent(PATH_PROPERTY_KEYS, s -> config.interpretPath(Path.of(s)));

        config.replaceIfPresent(KEY_NUMBER_OF_LEGS, Integer::parseInt);
        config.replaceIfPresent(KEY_RACE_START_TIME, Normalisation::parseTime);

        // Default entry map with elements (bib number, team name, category, plus one per leg), and no column combining or re-ordering.
        config.addIfAbsent(KEY_ENTRY_COLUMN_MAP, makeDefaultEntryColumnMap((int) config.get(KEY_NUMBER_OF_LEGS) + 3));
    }
}

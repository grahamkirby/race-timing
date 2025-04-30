/*
 * Copyright 2025 Graham Kirby:
 * <https://github.com/grahamkirby/race-timing>
 *
 * This file is part of the module race-timing.
 *
 * race-timing is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * race-timing is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with race-timing. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.series_race;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.output.RaceOutputCSV;

import java.util.Objects;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Race.KEY_RACE_NAME_FOR_RESULTS;

public abstract class SeriesRaceOutputCSV extends RaceOutputCSV {

    private static final String OVERALL_RESULTS_HEADER = "Pos,Runner,Club,Category";

    protected SeriesRaceOutputCSV(final Race race) {
        super(race);
    }

    protected String getSeriesResultsHeader() {

        return STR."\{OVERALL_RESULTS_HEADER},\{((SeriesRace) race).getRaces().stream().
            filter(Objects::nonNull).
            map(race -> race.getRequiredProperty(KEY_RACE_NAME_FOR_RESULTS)).
            collect(Collectors.joining(","))}";
    }
}

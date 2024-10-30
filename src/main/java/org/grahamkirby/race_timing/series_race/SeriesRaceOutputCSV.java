/*
 * Copyright 2024 Graham Kirby:
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

import java.io.IOException;
import java.io.OutputStreamWriter;

import static org.grahamkirby.race_timing.common.Race.KEY_RACE_NAME_FOR_RESULTS;

public abstract class SeriesRaceOutputCSV extends RaceOutputCSV {

    public static final String OVERALL_RESULTS_HEADER = "Pos,Runner,Club,Category";

    public SeriesRaceOutputCSV(Race race) {
        super(race);
    }

    protected void printOverallResultsHeaderRootSeries(final OutputStreamWriter writer) throws IOException {

        writer.append(OVERALL_RESULTS_HEADER);

        for (final Race individual_race : ((SeriesRace)race).getRaces())
            if (individual_race != null)
                writer.append(",").append(individual_race.getProperty(KEY_RACE_NAME_FOR_RESULTS));
    }
}

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
package org.grahamkirby.race_timing.series_race.fife_ac_minitour;

import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.output.RaceOutputPDF;

import static org.grahamkirby.race_timing.common.Normalisation.format;

public class MinitourRaceOutputPDF extends RaceOutputPDF {

    public MinitourRaceOutputPDF(final MinitourRace race) {
        super(race);
    }

    @Override
    protected PrizeWinnerDetails getPrizeWinnerDetails(final RaceResult r) {

        final MinitourRaceResult result = (MinitourRaceResult) r;
        return new PrizeWinnerDetails(result.position_string, result.runner.name, result.runner.club, format(result.duration()));
    }
}

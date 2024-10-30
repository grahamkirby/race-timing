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
package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.output.RaceOutputPDF;

import static org.grahamkirby.race_timing.common.Normalisation.format;

public class IndividualRaceOutputPDF extends RaceOutputPDF {

    public IndividualRaceOutputPDF(final IndividualRace results) {
        super(results);
    }

    @Override
    protected PrizeWinnerDetails getPrizeWinnerDetails(final RaceResult r) {

        final IndividualRaceResult result = ((IndividualRaceResult) r);
        return new PrizeWinnerDetails(result.position_string, result.entry.runner.name, result.entry.runner.club, format(result.duration()));
    }
}

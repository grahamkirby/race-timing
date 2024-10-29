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
import org.grahamkirby.race_timing.common.output.RaceOutputText;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import static org.grahamkirby.race_timing.common.Normalisation.format;

public class IndividualRaceOutputText extends RaceOutputText {

    public IndividualRaceOutputText(final IndividualRace race) {
        super(race);
    }

    protected void printPrizes(final OutputStreamWriter writer, final List<RaceResult> category_prize_winners) throws IOException {

        int position = 1;

        for (final RaceResult r : category_prize_winners) {

            final IndividualRaceResult result = ((IndividualRaceResult) r);

            writer.append(String.valueOf(position++)).append(": ").
                    append(result.entry.runner.name).append(" (").
                    append(result.entry.runner.club).append(") ").
                    append(format(result.duration())).append("\n");
        }
    }
}

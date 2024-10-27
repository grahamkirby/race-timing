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

import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.output.RaceOutputText;
import org.grahamkirby.race_timing.common.RaceResult;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import static org.grahamkirby.race_timing.common.Normalisation.format;

public class IndividualRaceOutputText extends RaceOutputText {

    public IndividualRaceOutputText(final IndividualRace race) {
        super(race);
    }

    @Override
    public void printPrizes(final OutputStreamWriter writer, final PrizeCategory category) throws IOException {

        final List<RaceResult> category_prize_winners = race.prize_winners.get(category);

        if (category_prize_winners != null) {

            final String header = "Category: " + category.getLongName();

            writer.append(header).append("\n");
            writer.append("-".repeat(header.length())).append("\n\n");

            if (category_prize_winners.isEmpty())
                writer.append("No results\n");
            else
                printPrizes(writer, category_prize_winners);

            writer.append("\n\n");
        }
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

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


import org.grahamkirby.race_timing.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.ResultPrinterText;
import org.grahamkirby.race_timing.series_race.SeriesRaceOutputText;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import static org.grahamkirby.race_timing.common.Config.*;

@SuppressWarnings("preview")
public class IndividualRaceOutputText {

    private final Race race;

    protected IndividualRaceOutputText(final Race race) {
        this.race = race;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    void printPrizes() throws IOException {

        final OutputStream stream = IndividualRaceResultsOutput.getOutputStream(race, "prizes", TEXT_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(SeriesRaceOutputText.getPrizesHeader(race));
            SeriesRaceOutputText.printPrizes(writer, race);
            printTeamPrizes(writer);
        }
    }

    /** Prints out the words converted to title case, and any other processing notes. */
    void printNotes() throws IOException {

        SeriesRaceOutputText.printNotes(race);
    }

    private void printTeamPrizes(final OutputStreamWriter writer) throws IOException {

        final List<String> team_prizes = ((IndividualRaceImpl) race.getSpecific()).getTeamPrizes();

        if (!team_prizes.isEmpty()) {

            writer.append("Team Prizes\n");
            writer.append("-----------\n\n");

            for (final String team_prize : team_prizes) {
                writer.append(team_prize);
                writer.append(LINE_SEPARATOR);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static final class PrizeResultPrinter extends ResultPrinterText {

        public PrizeResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult result) throws IOException {

            writer.append(result.getPositionString() + ": " + result.getParticipantName() + " " + result.getPrizeDetailText() + LINE_SEPARATOR);
        }
    }
}

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


import org.grahamkirby.race_timing.common.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;

import static org.grahamkirby.race_timing.common.Config.*;

public class IndividualRaceOutputCSV {

    private static final String OVERALL_RESULTS_HEADER = "Pos,No,Runner,Club,Category,Time" + LINE_SEPARATOR;
    private final Race race;

    IndividualRaceOutputCSV(final Race race) {
        this.race = race;
    }

    void printResults() throws IOException {

        final String race_name = (String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES);
        final String year = (String) race.getConfig().get(KEY_YEAR);

        final OutputStream stream = Files.newOutputStream(race.getOutputDirectoryPath().resolve(race_name + "_overall_" + year + "." + CSV_FILE_SUFFIX), STANDARD_FILE_OPEN_OPTIONS);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(OVERALL_RESULTS_HEADER);
            IndividualRaceResultsOutput.printResults(writer, new OverallResultPrinter(race, writer), _ -> "", race);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OverallResultPrinter extends ResultPrinter {

        // TODO investigate Files.write.
        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        public void printResult(final RaceResult r) throws IOException {

            final SingleRaceResult result = (SingleRaceResult) r;
            final RaceEntry entry = result.entry;
            final Participant participant = entry.participant;

            writer.append(result.getPositionString() + "," + entry.bib_number + "," + encode(participant.name) + ",");
            writer.append(encode(((Runner) participant).club) + "," + participant.category.getShortName() + "," + renderDuration(result, DNF_STRING) + LINE_SEPARATOR);
        }
    }
}

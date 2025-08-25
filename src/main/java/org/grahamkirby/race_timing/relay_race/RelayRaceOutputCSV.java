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


import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Team;
import org.grahamkirby.race_timing.common.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing.common.output.RaceOutputCSV;
import org.grahamkirby.race_timing.common.output.ResultPrinter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import static org.grahamkirby.race_timing_experimental.common.Config.DNF_STRING;
import static org.grahamkirby.race_timing_experimental.common.Config.LINE_SEPARATOR;

class RelayRaceOutputCSV extends RaceOutputCSV {

    private static final String OVERALL_RESULTS_HEADER = "Pos,No,Team,Category,";

    //////////////////////////////////////////////////////////////////////////////////////////////////

    RelayRaceOutputCSV(final Race race) {
        super(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getResultsHeader() {
        return STR."\{OVERALL_RESULTS_HEADER}Total\{LINE_SEPARATOR}";
    }

    @Override
    protected ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer) {
        return new OverallResultPrinter(race, writer);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    void printDetailedResults() throws IOException {

        try (final OutputStreamWriter writer = new OutputStreamWriter(getOutputStream(race_name_for_filenames, "detailed", year))) {

            printDetailedResultsHeader(writer);
            printDetailedResults(writer);
        }
    }

    private void printDetailedResultsHeader(final OutputStreamWriter writer) throws IOException {

        final int number_of_legs = ((RelayRace) race).getNumberOfLegs();

        writer.append(OVERALL_RESULTS_HEADER);

        for (int leg_number = 1; leg_number <= number_of_legs; leg_number++) {

            writer.append(STR."Runners \{leg_number},Leg \{leg_number},");
            if (leg_number < number_of_legs) writer.append(STR."Split \{leg_number},");
        }

        writer.append("Total").append(LINE_SEPARATOR);
    }

    private void printDetailedResults(final OutputStreamWriter writer) throws IOException {

        final ResultPrinter printer = new DetailedResultPrinter(race, writer);

        for (final PrizeCategoryGroup group : race.prize_category_groups)
            printer.print(race.getOverallResults(group.categories()));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    void printLegResults() throws IOException {

        for (int leg = 1; leg <= ((RelayRace) race).getNumberOfLegs(); leg++)
            printLegResults(leg);
    }

    private void printLegResults(final int leg) throws IOException {

        try (final OutputStreamWriter writer = new OutputStreamWriter(getOutputStream(race_name_for_filenames, STR."leg_\{leg}", year))) {
            printLegResults(writer, leg);
        }
    }

    private void printLegResults(final OutputStreamWriter writer, final int leg) throws IOException {

        final List<LegResult> leg_results = ((RelayRace) race).getLegResults(leg);

        new LegResultPrinter(race, writer, leg).print(leg_results);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    // Prize results not printed to text file.
    @Override
    protected ResultPrinter getPrizeResultPrinter(final OutputStreamWriter writer) {
        throw new UnsupportedOperationException();
    }

    private static final class OverallResultPrinter extends ResultPrinter {

        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final RelayRaceResult result = (RelayRaceResult) r;
            writer.append(STR."\{result.position_string},\{result.entry.bib_number},\{encode(result.entry.participant.name)},\{result.entry.participant.category.getShortName()},\{renderDuration(result, DNF_STRING)}\n");
        }
    }

    private static final class LegResultPrinter extends ResultPrinter {

        final int leg;

        private LegResultPrinter(final Race race, final OutputStreamWriter writer, final int leg) {

            super(race, writer);
            this.leg = leg;
        }

        @Override
        public void printResultsHeader() throws IOException {

            final String plural = ((RelayRace) race).getPairedLegs().get(leg - 1) ? "s" : "";
            writer.append(STR."Pos,Runner\{plural},Time\n");
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final LegResult result = (LegResult) r;
            final String runner_names = encode(((Team) result.entry.participant).runner_names.get(result.leg_number - 1));

            writer.append(STR."\{result.position_string},\{runner_names},\{renderDuration(result, DNF_STRING)}\n");
        }
    }

    private static final class DetailedResultPrinter extends ResultPrinter {

        private DetailedResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final RelayRace relay_race = (RelayRace) race;
            final RelayRaceResult result = (RelayRaceResult) r;

            writer.append(STR."\{result.position_string},\{result.entry.bib_number},\{encode(result.entry.participant.name)},\{result.entry.participant.category.getLongName()},");

            final List<String> leg_strings = relay_race.getLegDetails(result).stream().
                map(RaceOutputCSV::encode).toList();

            writer.append(String.join(",", leg_strings));
            writer.append(LINE_SEPARATOR);
        }
    }
}

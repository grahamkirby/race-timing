/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (graham.kirby@st-andrews.ac.uk)
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
import org.grahamkirby.race_timing.common.output.RaceOutputHTML;
import org.grahamkirby.race_timing.common.output.ResultPrinter;
import org.grahamkirby.race_timing.common.output.ResultPrinterHTML;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static org.grahamkirby.race_timing.common.Race.LINE_SEPARATOR;

class RelayRaceOutputHTML extends RaceOutputHTML {

    RelayRaceOutputHTML(final RelayRace race) {

        super(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void printCombinedDetails() throws IOException {

        super.printCombinedDetails();

        final OutputStream stream = getOutputStream(race_name_for_filenames, "combined", year, StandardOpenOption.APPEND);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append("<h4>Full Results</h4>").append(LINE_SEPARATOR);
            printDetailedResults(writer);

            for (int leg_number = 1; leg_number <= ((RelayRace) race).getNumberOfLegs(); leg_number++) {

                writer.append(STR."<p></p>\{LINE_SEPARATOR}<h4>Leg \{leg_number} Results</h4>\{LINE_SEPARATOR}");
                printLegResults(writer, leg_number);
            }
        }
    }

    @Override
    protected ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer) {
        return new OverallResultPrinter(race, writer);
    }

    @Override
    protected ResultPrinter getPrizeResultPrinter(final OutputStreamWriter writer) {
        return new PrizeResultPrinter(race, writer);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    void printDetailedResults() throws IOException {

        final OutputStream stream = getOutputStream(race_name_for_filenames, "detailed", year);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            printDetailedResults(writer);
        }
    }

    private void printDetailedResults(final OutputStreamWriter writer) throws IOException {

        printResults(writer, new DetailedResultPrinter(race, writer));

        if (areAnyResultsInMassStart())
            writer.append("<p>M3: mass start leg 3<br />M4: mass start leg 4</p>").append(LINE_SEPARATOR);
    }

    private boolean areAnyResultsInMassStart() {

        return race.getOverallResults().stream().
            map(result -> (RelayRaceResult) result).
            flatMap(result -> result.leg_results.stream()).
            anyMatch(result -> result.in_mass_start);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    void printLegResults() throws IOException {

        for (int leg = 1; leg <= ((RelayRace) race).getNumberOfLegs(); leg++)
            printLegResults(leg);
    }

    private void printLegResults(final int leg) throws IOException {

        final OutputStream stream = getOutputStream(race_name_for_filenames, STR."leg_\{leg}", year);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            printLegResults(writer, leg);
        }
    }

    private void printLegResults(final OutputStreamWriter writer, final int leg) throws IOException {

        final List<LegResult> leg_results = ((RelayRace) race).getLegResults(leg);

        new LegResultPrinter(race, writer, leg).print(leg_results);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OverallResultPrinter extends ResultPrinterHTML {

        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected List<String> getResultsColumnHeaders() {

            return List.of("Pos", "No", "Team", "Category", "Total");
        }

        @Override
        protected List<String> getResultsElements(final RaceResult r) {

            final RelayRaceResult result = ((RelayRaceResult) r);

            return List.of(
                result.position_string,
                String.valueOf(result.entry.bib_number),
                race.normalisation.htmlEncode(result.entry.participant.name),
                result.entry.participant.category.getLongName(),
                renderDuration(result, DNF_STRING)
            );
        }
    }

    private static final class LegResultPrinter extends ResultPrinterHTML {

        final int leg;

        private LegResultPrinter(final Race race, final OutputStreamWriter writer, final int leg) {

            super(race, writer);
            this.leg = leg;
        }

        @Override
        protected List<String> getResultsColumnHeaders() {

            return List.of(
                "Pos",
                STR."Runner\{((RelayRace) race).getPairedLegs().get(leg - 1) ? "s" : ""}",
                "Time");
        }

        @Override
        protected List<String> getResultsElements(final RaceResult r) {

            final LegResult leg_result = (LegResult) r;

            return List.of(
                leg_result.position_string,
                race.normalisation.htmlEncode(((Team) leg_result.entry.participant).runner_names.get(leg_result.leg_number - 1)),
                renderDuration(leg_result, DNF_STRING)
            );
        }
    }

    private static final class DetailedResultPrinter extends ResultPrinterHTML {

        private DetailedResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected List<String> getResultsColumnHeaders() {

            final List<String> headers = new ArrayList<>(List.of("Pos", "No", "Team", "Category"));

            for (int leg_number = 1; leg_number <= ((RelayRace) race).getNumberOfLegs(); leg_number++) {

                headers.add(STR."Runner\{((RelayRace) race).getPairedLegs().get(leg_number - 1) ? "s" : ""} \{leg_number}");
                headers.add(STR."Leg \{leg_number}");
                headers.add(leg_number < ((RelayRace) race).getNumberOfLegs() ? STR."Split \{leg_number}" : "Total");
            }

            return headers;
        }

        @Override
        protected List<String> getResultsElements(final RaceResult r) {

            final List<String> elements = new ArrayList<>();

            final RelayRace relay_race = (RelayRace) race;
            final RelayRaceResult result = (RelayRaceResult) r;

            elements.add(result.position_string);
            elements.add(String.valueOf(result.entry.bib_number));
            elements.add(race.normalisation.htmlEncode(result.entry.participant.name));
            elements.add(result.entry.participant.category.getLongName());

            for (final String element : relay_race.getLegDetails(result))
                elements.add(race.normalisation.htmlEncode(element));

            return elements;
        }
    }

    private static final class PrizeResultPrinter extends ResultPrinterHTML {

        private PrizeResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {

            writer.append("<ul>").append(LINE_SEPARATOR);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final RelayRaceResult result = (RelayRaceResult) r;

            writer.append(STR."""
                    <li>\{result.position_string} \{race.normalisation.htmlEncode(result.entry.participant.name)} (\{result.entry.participant.category.getLongName()}) \{renderDuration(result)}</li>
                """);
        }

        @Override
        public void printResultsFooter() throws IOException {

            writer.append("</ul>").append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        }
    }
}

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
import org.grahamkirby.race_timing.common.ResultPrinterHTML;
import org.grahamkirby.race_timing.individual_race.IndividualRaceResultsOutput;
import org.grahamkirby.race_timing.individual_race.IndividualResultPrinterHTML;
import org.grahamkirby.race_timing.series_race.SeriesRaceOutputHTML;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.grahamkirby.race_timing.common.Config.*;

public class RelayRaceOutputHTML {

    private final Race race;

    RelayRaceOutputHTML(final Race race) {
        this.race = race;
    }

    void printResults() throws IOException {

        IndividualRaceResultsOutput.printResults(race, OverallResultPrinter::new);
    }

    /** Prints all details to a single web page. */
    void printCombined() throws IOException {

        final String race_name = (String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES);
        final String year = (String) race.getConfig().get(KEY_YEAR);

        try (final OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(getOutputFilePath(race_name, "combined", year), STANDARD_FILE_OPEN_OPTIONS))) {

            writer.append("<h3>Results</h3>").append(LINE_SEPARATOR);

            writer.append(SeriesRaceOutputHTML.getPrizesHeader());
            printPrizes(writer);

            writer.append("<h4>Overall</h4>").append(LINE_SEPARATOR);
            IndividualRaceResultsOutput.printResults(writer, new OverallResultPrinter(race, writer), SeriesRaceOutputHTML::getResultsSubHeader, race);

            writer.append("<h4>Full Results</h4>").append(LINE_SEPARATOR);
            printDetailedResults(writer);

            for (int leg_number = 1; leg_number <= ((RelayRaceImpl) race.getSpecific()).getNumberOfLegs(); leg_number++) {

                writer.append("<p></p>" + LINE_SEPARATOR + "<h4>Leg " + leg_number + " Results</h4>" + LINE_SEPARATOR);
                printLegResults(writer, leg_number);
            }

            writer.append(SOFTWARE_CREDIT_LINK_TEXT);
        }
    }

    void printPrizes() throws IOException {

        try (final OutputStreamWriter writer = new OutputStreamWriter(IndividualRaceResultsOutput.getOutputStream(race, "prizes"))) {

            writer.append(SeriesRaceOutputHTML.getPrizesHeader());
            printPrizes(writer);
        }
    }

    void printDetailedResults() throws IOException {

        final String race_name = (String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES);
        final String year = (String) race.getConfig().get(KEY_YEAR);

        try (final OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(getOutputFilePath(race_name, "detailed", year), STANDARD_FILE_OPEN_OPTIONS))) {
            printDetailedResults(writer);
        }
    }

    void printLegResults() throws IOException {

        for (int leg = 1; leg <= ((RelayRaceImpl) race.getSpecific()).getNumberOfLegs(); leg++)
            printLegResults(leg);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Prints prizes, ordered by prize category groups. */
    private void printPrizes(final OutputStreamWriter writer) {

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).           // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory).       // Ignore further categories once all prizes have been output.
            forEachOrdered(category -> SeriesRaceOutputHTML.printPrizes(writer, category, race, PrizeResultPrinter::new));    // Print prizes in this category.
    }

    /**
     * Constructs a path for a file in the project output directory with name constructed from the given components.
     */
    private Path getOutputFilePath(final String race_name, final String output_type, final String year) {

        return race.getOutputDirectoryPath().resolve(race_name + "_" + output_type + "_" + year + "." + HTML_FILE_SUFFIX);
    }

    private void printDetailedResults(final OutputStreamWriter writer) throws IOException {

        IndividualRaceResultsOutput.printResults(writer, new DetailedResultPrinter(race, writer), SeriesRaceOutputHTML::getResultsSubHeader, race);

        if (areAnyResultsInMassStart())
            writer.append("<p>M3: mass start leg 3<br />M4: mass start leg 4</p>").append(LINE_SEPARATOR);
    }

    private boolean areAnyResultsInMassStart() {

        return race.getResultsCalculator().getOverallResults().stream().
            map(result -> (RelayRaceResult) result).
            flatMap(result -> result.leg_results.stream()).
            anyMatch(result -> result.in_mass_start);
    }

    private void printLegResults(final int leg) throws IOException {

        final String race_name = (String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES);
        final String output_type = "leg_" + leg;
        final String year = (String) race.getConfig().get(KEY_YEAR);

        final OutputStream stream = Files.newOutputStream(getOutputFilePath(race_name, output_type, year), STANDARD_FILE_OPEN_OPTIONS);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            printLegResults(writer, leg);
        }
    }

    private void printLegResults(final OutputStreamWriter writer, final int leg) throws IOException {

        final List<LegResult> leg_results = ((RelayRaceImpl) race.getSpecific()).getLegResults(leg);

        new LegResultPrinter(race, writer, leg).print(leg_results);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OverallResultPrinter extends IndividualResultPrinterHTML {

        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected List<String> getResultsColumnHeaders() {

            return List.of("Pos", "No", "Team", "Category", "Total");
        }

        @Override
        protected List<String> getResultsElements(final RaceResult r) {

            final RelayRaceResult result = (RelayRaceResult) r;

            return List.of(
                result.position_string,
                String.valueOf(result.entry.bib_number),
                race.getNormalisation().htmlEncode(result.entry.participant.name),
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
                "Runner" + (((RelayRaceImpl) race.getSpecific()).getPairedLegs().get(leg - 1) ? "s" : ""),
                "Time");
        }

        @Override
        protected List<String> getResultsElements(final RaceResult r) {

            final LegResult leg_result = (LegResult) r;

            return List.of(
                leg_result.position_string,
                race.getNormalisation().htmlEncode(((Team) leg_result.entry.participant).runner_names.get(leg_result.leg_number - 1)),
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
            final RelayRaceImpl race_impl = (RelayRaceImpl) race.getSpecific();

            for (int leg_number = 1; leg_number <= race_impl.getNumberOfLegs(); leg_number++) {

                headers.add("Runner" + (race_impl.getPairedLegs().get(leg_number - 1) ? "s" : "") + " " + leg_number);
                headers.add("Leg " + leg_number);
                headers.add(leg_number < race_impl.getNumberOfLegs() ? "Split " + leg_number : "Total");
            }

            return headers;
        }

        @Override
        protected List<String> getResultsElements(final RaceResult r) {

            final List<String> elements = new ArrayList<>();

            final RelayRaceResult result = (RelayRaceResult) r;

            elements.add(result.position_string);
            elements.add(String.valueOf(result.entry.bib_number));
            elements.add(race.getNormalisation().htmlEncode(result.entry.participant.name));
            elements.add(result.entry.participant.category.getLongName());

            for (final String element : ((RelayRaceImpl) race.getSpecific()).getLegDetails(result))
                elements.add(race.getNormalisation().htmlEncode(element));

            return elements;
        }
    }

    private static final class PrizeResultPrinter extends IndividualResultPrinterHTML {

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

            writer.append("    <li>" + result.position_string + " " + race.getNormalisation().htmlEncode(result.entry.participant.name) + " (" + result.entry.participant.category.getLongName() + ") " + renderDuration(result, DNF_STRING) + "</li>" + LINE_SEPARATOR);
        }

        @Override
        public void printResultsFooter() throws IOException {

            writer.append("</ul>").append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        }
    }
}

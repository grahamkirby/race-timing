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


import org.grahamkirby.race_timing.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing.common.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.Normalisation.renderDuration;

public class RelayRaceOutput extends RaceOutput {

    private static final String OVERALL_RESULTS_HEADER = "Pos,No,Team,Category,";

    @Override
    public void outputResults() throws IOException {

        printOverallResults();
        printDetailedResults();
        printLegResults();
        printCollatedTimes();

        printPrizes();
        printNotes();
        printCombined();
    }

    @Override
    public void setRace(final Race race) {

        this.race = race;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void printOverallResults() throws IOException {

        printResultsCSV();
        printResultsHTML();
    }

    private void printDetailedResults() throws IOException {

        printDetailedResultsCSV();
        printDetailedResultsHTML();
    }

    private void printLegResults() throws IOException {

        printLegResultsCSV();
        printLegResultsHTML();
    }

    private void printCollatedTimes() throws IOException {

        printCollatedResultsText();
    }

    private void printPrizes() throws IOException {

        printPrizesPDF();
        printPrizesHTML();
        printPrizesText();
    }

    private void printCombined() throws IOException {

        printCombinedHTML();
    }

    void printResultsCSV() throws IOException {

        final OutputStream stream = getOutputStream("overall", CSV_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            printResultsCSV(writer, new OverallResultPrinterCSV(race, writer));
        }
    }

    void printDetailedResultsCSV() throws IOException {

        final OutputStream stream = getOutputStream("detailed", CSV_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            printResultsCSV(writer, new DetailedResultPrinterCSV(race, writer));
        }
    }

    void printLegResultsCSV() throws IOException {

        for (int leg = 1; leg <= ((RelayRaceImpl) race.getSpecific()).getNumberOfLegs(); leg++)
            printLegResultsCSV(leg);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void printLegResultsCSV(final int leg) throws IOException {

        final OutputStream stream = getOutputStream("leg_" + leg, CSV_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            final List<LegResult> leg_results = ((RelayRaceImpl) race.getSpecific()).getLegResults(leg);
            new LegResultPrinterCSV(race, writer, leg).print(leg_results);
        }
    }

    /** Prints results using a specified printer, ordered by prize category groups. */
    private void printResultsCSV(final OutputStreamWriter writer, final ResultPrinter printer) throws IOException {

        // Don't display category group headers if there is only one group.
        final boolean should_display_category_group_headers = race.getCategoryDetails().getPrizeCategoryGroups().size() > 1;

        boolean not_first_category_group = false;

        for (final PrizeCategoryGroup group : race.getCategoryDetails().getPrizeCategoryGroups()) {

            if (should_display_category_group_headers && not_first_category_group)
                writer.append(LINE_SEPARATOR);

            printer.print(race.getResultsCalculator().getOverallResults(group.categories()));

            not_first_category_group = true;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OverallResultPrinterCSV extends ResultPrinter {

        private OverallResultPrinterCSV(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {

            writer.append(OVERALL_RESULTS_HEADER + "Total" + LINE_SEPARATOR);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final RelayRaceResult result = (RelayRaceResult) r;
            writer.append(result.getPositionString() + "," + result.bib_number + "," + encode(result.getParticipantName()) + "," + result.getParticipant().category.getShortName() + "," + renderDuration(result, DNF_STRING) + LINE_SEPARATOR);
        }
    }

    private static final class DetailedResultPrinterCSV extends ResultPrinter {

        private DetailedResultPrinterCSV(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {

            final int number_of_legs = ((RelayRaceImpl)race.getSpecific()).getNumberOfLegs();

            writer.append(OVERALL_RESULTS_HEADER);

            for (int leg_number = 1; leg_number <= number_of_legs; leg_number++) {

                writer.append("Runners " + leg_number + ",Leg " + leg_number + ",");
                if (leg_number < number_of_legs) writer.append("Split " + leg_number + ",");
            }

            writer.append("Total").append(LINE_SEPARATOR);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final RelayRaceResult result = (RelayRaceResult) r;

            writer.append(result.getPositionString() + "," + result.bib_number + "," + encode(result.getParticipantName()) + "," + result.getParticipant().category.getLongName() + ",");

            final String leg_details = ((RelayRaceImpl) race.getSpecific()).getLegDetails(result).stream().
                map(Config::encode).
                collect(Collectors.joining(","));

            writer.append(leg_details);
            writer.append(LINE_SEPARATOR);
        }
    }

    private static final class LegResultPrinterCSV extends ResultPrinter {

        final int leg;

        private LegResultPrinterCSV(final Race race, final OutputStreamWriter writer, final int leg) {

            super(race, writer);
            this.leg = leg;
        }

        @Override
        public void printResultsHeader() throws IOException {

            final String plural = ((RelayRaceImpl) race.getSpecific()).getPairedLegs().get(leg - 1) ? "s" : "";
            writer.append("Pos,Runner" + plural + ",Time" + LINE_SEPARATOR);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final LegResult result = (LegResult) r;
            final String runner_names = encode(((Team) result.getParticipant()).runner_names.get(result.leg_number - 1));

            writer.append(result.getPositionString() + "," + runner_names + "," + renderDuration(result, DNF_STRING) + LINE_SEPARATOR);
        }
    }

    void printResultsHTML() throws IOException {

        printResults(OverallResultPrinter::new);
    }

    /** Prints all details to a single web page. */
    void printCombinedHTML() throws IOException {

        final OutputStream stream = getOutputStream("combined", HTML_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append("<h3>Results</h3>").append(LINE_SEPARATOR);

            writer.append(getPrizesHeaderHTML());
            printPrizesHTML(writer, PrizeResultPrinter::new);

            writer.append("<h4>Overall</h4>").append(LINE_SEPARATOR);
            printResults(writer, new OverallResultPrinter(race, writer), this::getResultsSubHeaderHTML);

            writer.append("<h4>Full Results</h4>").append(LINE_SEPARATOR);
            printDetailedResultsHTML(writer);

            for (int leg_number = 1; leg_number <= ((RelayRaceImpl) race.getSpecific()).getNumberOfLegs(); leg_number++) {

                writer.append("<p></p>" + LINE_SEPARATOR + "<h4>Leg " + leg_number + " Results</h4>" + LINE_SEPARATOR);
                printLegResultsHTML(writer, leg_number);
            }

            writer.append(SOFTWARE_CREDIT_LINK_TEXT);
        }
    }

    public void printPrizesHTML() throws IOException {

        final OutputStream stream = getOutputStream("prizes", HTML_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(getPrizesHeaderHTML());
            printPrizesHTML(writer, PrizeResultPrinter::new);
        }
    }

    void printDetailedResultsHTML() throws IOException {

        final OutputStream stream = getOutputStream("detailed", HTML_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            printDetailedResultsHTML(writer);
        }
    }

    void printLegResultsHTML() throws IOException {

        for (int leg = 1; leg <= ((RelayRaceImpl) race.getSpecific()).getNumberOfLegs(); leg++)
            printLegResultsHTML(leg);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void printDetailedResultsHTML(final OutputStreamWriter writer) throws IOException {

        printResults(writer, new DetailedResultPrinter(race, writer), this::getResultsSubHeaderHTML);

        if (areAnyResultsInMassStart())
            writer.append("<p>M3: mass start leg 3<br />M4: mass start leg 4</p>").append(LINE_SEPARATOR);
    }

    private boolean areAnyResultsInMassStart() {

        return race.getResultsCalculator().getOverallResults().stream().
            map(result -> (RelayRaceResult) result).
            flatMap(result -> result.leg_results.stream()).
            anyMatch(result -> result.in_mass_start);
    }

    private void printLegResultsHTML(final int leg) throws IOException {

        final OutputStream stream = getOutputStream("leg_" + leg, HTML_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            printLegResultsHTML(writer, leg);
        }
    }

    private void printLegResultsHTML(final OutputStreamWriter writer, final int leg) throws IOException {

        final List<LegResult> leg_results = ((RelayRaceImpl) race.getSpecific()).getLegResults(leg);

        new LegResultPrinter(race, writer, leg).print(leg_results);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OverallResultPrinter extends OverallResultPrinterHTML {

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
                result.getPositionString(),
                String.valueOf(result.bib_number),
                race.getNormalisation().htmlEncode(result.getParticipant().name),
                result.getParticipant().category.getLongName(),
                renderDuration(result, DNF_STRING)
            );
        }
    }

    private static final class LegResultPrinter extends OverallResultPrinterHTML {

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
                leg_result.getPositionString(),
                race.getNormalisation().htmlEncode(((Team) leg_result.getParticipant()).runner_names.get(leg_result.leg_number - 1)),
                renderDuration(leg_result, DNF_STRING)
            );
        }
    }

    private static final class DetailedResultPrinter extends OverallResultPrinterHTML {

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

            elements.add(result.getPositionString());
            elements.add(String.valueOf(result.bib_number));
            elements.add(race.getNormalisation().htmlEncode(result.getParticipantName()));
            elements.add(result.getParticipant().category.getLongName());

            for (final String element : ((RelayRaceImpl) race.getSpecific()).getLegDetails(result))
                elements.add(race.getNormalisation().htmlEncode(element));

            return elements;
        }
    }

    public static final class PrizeResultPrinter extends PrizeResultPrinterHTML {

        public PrizeResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected String renderDetail(final RaceResult result) {
            return result.getParticipant().category.getLongName();
        }

        @Override
        protected String renderPerformance(final RaceResult result) {
            return renderDuration((RaceResultWithDuration) result, DNF_STRING);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    void printCollatedResultsText() throws IOException {

        final OutputStream stream = getOutputStream("times_collated", TEXT_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            final RelayRaceImpl impl = (RelayRaceImpl) race.getSpecific();

            final Map<Integer, Integer> legs_finished_per_team = impl.countLegsFinishedPerTeam();
            final List<Integer> bib_numbers_with_missing_times = impl.getBibNumbersWithMissingTimes(legs_finished_per_team);
            final List<Duration> times_with_missing_bib_numbers = impl.getTimesWithMissingBibNumbers();

            printResults(writer, legs_finished_per_team);

            final boolean discrepancies_exist = !bib_numbers_with_missing_times.isEmpty() || !times_with_missing_bib_numbers.isEmpty();

            if (discrepancies_exist)
                race.appendToNotes("""
                
                Discrepancies:
                -------------
                """);

            printBibNumbersWithMissingTimes(bib_numbers_with_missing_times);
            printTimesWithMissingBibNumbers(times_with_missing_bib_numbers);

            if (discrepancies_exist)
                race.appendToNotes("""
                
                
                """);
        }
    }

    private void printResults(final OutputStreamWriter writer, final Map<Integer, Integer> legs_finished_per_team) throws IOException {

        for (final RawResult result : race.getRaceData().getRawResults()) {

            final int legs_already_finished = legs_finished_per_team.get(result.getBibNumber()) - 1;
            printResult(writer, result, legs_already_finished);
        }
    }

    private void printResult(final OutputStreamWriter writer, final RawResult raw_result, final int legs_already_finished) throws IOException {

        printBibNumberAndTime(writer, raw_result);
        printLegNumber(writer, raw_result, legs_already_finished);
        printComment(writer, raw_result);
    }

    private void printBibNumberAndTime(final OutputStreamWriter writer, final RawResult raw_result) throws IOException {

        final int bib_number = raw_result.getBibNumber();

        writer.append(bib_number != UNKNOWN_BIB_NUMBER ? String.valueOf(bib_number) : "?").
            append("\t").
            append(raw_result.getRecordedFinishTime() != null ? renderDuration(raw_result.getRecordedFinishTime()) : "?");
    }

    private void printLegNumber(final OutputStreamWriter writer, final RawResult raw_result, final int legs_already_finished) throws IOException {

        final Map<RawResult, Integer> explicitly_recorded_leg_numbers = ((RelayRaceDataImpl) race.getRaceData()).explicitly_recorded_leg_numbers;

        if (explicitly_recorded_leg_numbers.containsKey(raw_result)) {

            final int leg_number = explicitly_recorded_leg_numbers.get(raw_result);

            writer.append("\t").append(String.valueOf(leg_number));

            if (legs_already_finished >= leg_number)
                raw_result.appendComment("Leg " + leg_number + " finisher was runner " + (legs_already_finished + 1) + " to finish for team.");
        }
    }

    private void printComment(final OutputStreamWriter writer, final RawResult raw_result) throws IOException {

        final Map<RawResult, Integer> explicitly_recorded_leg_numbers = ((RelayRaceDataImpl) race.getRaceData()).explicitly_recorded_leg_numbers;

        if (!raw_result.getComment().isEmpty()) {

            if (!explicitly_recorded_leg_numbers.containsKey(raw_result)) writer.append("\t");
            writer.append("\t").append(COMMENT_SYMBOL).append(" ").append(raw_result.getComment());
        }

        writer.append(LINE_SEPARATOR);
    }

    @SuppressWarnings("IncorrectFormatting")
    private void printBibNumbersWithMissingTimes(final List<Integer> bib_numbers_with_missing_times) {

        if (!bib_numbers_with_missing_times.isEmpty()) {

            race.appendToNotes("""
                
                Bib numbers with missing times:\s""");

            race.appendToNotes(
                bib_numbers_with_missing_times.stream().
                    map(String::valueOf).
                    collect(Collectors.joining(", ")));
        }
    }

    @SuppressWarnings("IncorrectFormatting")
    private void printTimesWithMissingBibNumbers(final List<Duration> times_with_missing_bib_numbers) {

        if (!times_with_missing_bib_numbers.isEmpty()) {

            race.appendToNotes("""
                
                Times with missing bib numbers:
                
                """);

            race.appendToNotes(
                times_with_missing_bib_numbers.stream().
                    map(Normalisation::renderDuration).
                    collect(Collectors.joining(LINE_SEPARATOR)));
        }
    }
}

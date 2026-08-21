/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2026 Graham Kirby (race-timing@kirby-family.net)
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


import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import org.grahamkirby.race_timing.common.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.NormalisationProcessor.csvEncode;
import static org.grahamkirby.race_timing.common.NormalisationProcessor.renderDuration;

public class RelayRaceOutput extends RaceOutput {

    public RelayRaceOutput(final Config config) {
        super(config);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void outputResults(final RaceResults results) throws IOException {

        race_results = results;

        printDetailedResults();
        printLegResults();
        printCollatedTimes();

        super.outputResults(results);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected ResultPrinterGenerator getOverallResultCSVPrinterGenerator() {
        return RelayRaceOverallResultPrinterCSV::new;
    }

    @Override
    protected ResultPrinterGenerator getOverallResultHTMLPrinterGenerator() {
        return RelayRaceOverallResultPrinterHTML::new;
    }

    @Override
    protected ResultPrinterGenerator getPrizeHTMLPrinterGenerator() {
        return PrizeResultPrinterHTML::new;
    }

    @Override
    protected BiFunction<RaceResults, Document, ResultPrinter> getPrizePDFPrinterGenerator() {
        return PrizeResultPrinterPDF::new;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

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

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void printDetailedResultsCSV() throws IOException {

        final OutputStream stream = getOutputStream(DETAILED, CSV_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            printResults(writer, new DetailedResultPrinterCSV(race_results, writer), _ -> "");
        }
    }

    private void printDetailedResultsHTML() throws IOException {

        final OutputStream stream = getOutputStream(DETAILED, HTML_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            printDetailedResultsHTML(writer);
        }
    }

    private void printDetailedResultsHTML(final OutputStreamWriter writer) throws IOException {

        printResults(writer, new DetailedResultPrinterHTML(race_results, writer), this::getResultsSubHeaderHTML);

        if (areAnyResultsInMassStart())
            writer.append("<p>" + M_3_MASS_START_LEG_3 + "<br />" + M_4_MASS_START_LEG_4 + "</p>").append(LINE_SEPARATOR);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void printLegResultsCSV() throws IOException {

        for (int leg = 1; leg <= ((RelayRaceResults) race_results).getNumberOfLegs(); leg++)
            printLegResultsCSV(leg);
    }

    private void printLegResultsCSV(final int leg) throws IOException {

        final OutputStream stream = getOutputStream(LEG + leg, CSV_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            final List<RelayRaceLegResult> leg_results = ((RelayRaceResults) race_results).getLegResults(leg);
            new LegResultPrinterCSV(race_results, writer, leg).print(leg_results);
        }
    }

    private void printLegResultsHTML() throws IOException {

        for (int leg = 1; leg <= ((RelayRaceResults) race_results).getNumberOfLegs(); leg++)
            printLegResultsHTML(leg);
    }

    private void printLegResultsHTML(final int leg) throws IOException {

        final OutputStream stream = getOutputStream(LEG + leg, HTML_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            printLegResultsHTML(writer, leg);
        }
    }

    private void printLegResultsHTML(final OutputStreamWriter writer, final int leg) throws IOException {

        final List<RelayRaceLegResult> leg_results = ((RelayRaceResults) race_results).getLegResults(leg);

        new LegResultPrinterHTML(race_results, writer, leg).print(leg_results);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Prints all details to a single web page. */
    protected void printCombinedHTML() throws IOException {

        final OutputStream stream = getOutputStream(COMBINED, HTML_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append("<h3>" + RESULTS + "</h3>").append(LINE_SEPARATOR);

            writer.append(getPrizesHeaderHTML());
            printPrizesHTML(writer, new PrizeResultPrinterHTML(race_results, writer));

            writer.append("<h4>" + OVERALL + "</h4>").append(LINE_SEPARATOR);
            printResults(writer, new RelayRaceOverallResultPrinterHTML(race_results, writer), this::getResultsSubHeaderHTML);

            writer.append("<h4>" + FULL_RESULTS + "</h4>").append(LINE_SEPARATOR);
            printDetailedResultsHTML(writer);

            for (int leg_number = 1; leg_number <= ((RelayRaceResults) race_results).getNumberOfLegs(); leg_number++) {

                writer.append("<p></p>" + LINE_SEPARATOR + "<h4>" + LEG1 + " " + leg_number + " " + RESULTS + "</h4>" + LINE_SEPARATOR);
                printLegResultsHTML(writer, leg_number);
            }

            writer.append(SOFTWARE_CREDIT_LINK_TEXT);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean areAnyResultsInMassStart() {

        return race_results.getOverallResults().stream().
            map(result -> (RelayRaceResult) result).
            flatMap(result -> result.getLegResults().stream()).
            anyMatch(RelayRaceLegResult::isInMassStart);
    }

    private void printResults(final OutputStreamWriter writer, final Map<Integer, Integer> legs_finished_per_team) throws IOException {

        for (final RawResult result : ((RelayRaceResults) race_results).getRawResults()) {

            final int legs_already_finished = legs_finished_per_team.get(result.getBibNumber()) - 1;
            printResult(writer, result, legs_already_finished);
        }
    }

    private void printResult(final OutputStreamWriter writer, final RawResult raw_result, final int legs_already_finished) throws IOException {

        printBibNumberAndTime(writer, raw_result);
        printLegNumber(writer, raw_result, legs_already_finished);
        printComment(writer, raw_result);
    }

    private void printCollatedResultsText() throws IOException {

        final OutputStream stream = getOutputStream(FILE_TIMES_COLLATED, TEXT_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            final Map<Integer, Integer> legs_finished_per_team = ((RelayRaceResults) race_results).countLegsFinishedPerTeam();

            printResults(writer, legs_finished_per_team);
            printNotes(legs_finished_per_team);
        }
    }

    private void printNotes(final Map<Integer, Integer> legs_finished_per_team) {

        final List<Integer> bib_numbers_with_missing_times = ((RelayRaceResults) race_results).getBibNumbersWithMissingTimes(legs_finished_per_team);
        final List<Duration> times_with_missing_bib_numbers = ((RelayRaceResults) race_results).getTimesWithMissingBibNumbers();

        final boolean discrepancies_exist = !bib_numbers_with_missing_times.isEmpty() || !times_with_missing_bib_numbers.isEmpty();

        if (discrepancies_exist)
            race_results.getNotesProcessor().appendToNotes(DISCREPANCIES);

        recordBibNumbersWithMissingTimes(bib_numbers_with_missing_times);
        recordTimesWithMissingBibNumbers(times_with_missing_bib_numbers);

        if (discrepancies_exist)
            race_results.getNotesProcessor().appendToNotes(S);
    }

    private void printBibNumberAndTime(final OutputStreamWriter writer, final RawResult raw_result) throws IOException {

        final int bib_number = raw_result.getBibNumber();

        writer.append(bib_number != UNKNOWN_BIB_NUMBER ? String.valueOf(bib_number) : UNKNOWN_BIB_NUMBER_INDICATOR).
            append(RAW_RESULT_SEPARATOR).
            append(raw_result.getRecordedFinishTime() != null ? renderDuration(raw_result.getRecordedFinishTime(), DNF_STRING) : UNKNOWN_TIME_INDICATOR);
    }

    private void printLegNumber(final OutputStreamWriter writer, final RawResult raw_result, final int legs_already_finished) throws IOException {

        final Map<RawResult, Integer> explicitly_recorded_leg_numbers = ((RelayRaceResults) race_results).getExplicitlyRecordedLegNumbers();

        if (explicitly_recorded_leg_numbers.containsKey(raw_result)) {

            final int leg_number = explicitly_recorded_leg_numbers.get(raw_result);
            writer.append(RAW_RESULT_SEPARATOR + leg_number);

            if (legs_already_finished >= leg_number)
                raw_result.appendComment(LEG1 + " " + leg_number + " " + FINISHER_WAS_RUNNER + " " + (legs_already_finished + 1) + " " + TO_FINISH_FOR_TEAM + ".");
        }
    }

    private void printComment(final OutputStreamWriter writer, final RawResult raw_result) throws IOException {

        final Map<RawResult, Integer> explicitly_recorded_leg_numbers = ((RelayRaceResults) race_results).getExplicitlyRecordedLegNumbers();

        if (!raw_result.getComment().isEmpty()) {

            if (!explicitly_recorded_leg_numbers.containsKey(raw_result)) writer.append(RAW_RESULT_SEPARATOR);
            writer.append(RAW_RESULT_SEPARATOR).append(COMMENT_SYMBOL).append(" ").append(raw_result.getComment());
        }

        writer.append(LINE_SEPARATOR);
    }

    private void recordBibNumbersWithMissingTimes(final List<Integer> bib_numbers_with_missing_times) {

        if (!bib_numbers_with_missing_times.isEmpty()) {

            race_results.getNotesProcessor().appendToNotes(S1).appendToNotes(
                bib_numbers_with_missing_times.stream().
                    map(String::valueOf).
                    collect(Collectors.joining(", ")));
        }
    }

    private void recordTimesWithMissingBibNumbers(final List<Duration> times_with_missing_bib_numbers) {

        if (!times_with_missing_bib_numbers.isEmpty()) {

            race_results.getNotesProcessor().appendToNotes(S2).appendToNotes(
                times_with_missing_bib_numbers.stream().
                    map(duration -> renderDuration(duration, DNF_STRING)).
                    collect(Collectors.joining(LINE_SEPARATOR)));
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class RelayRaceOverallResultPrinterCSV extends ResultPrinter {

        private RelayRaceOverallResultPrinterCSV(final RaceResults race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {

            writer.append(String.join(",", POS1) + "," + TOTAL + LINE_SEPARATOR);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final SingleRaceResult result = (SingleRaceResult) r;

            writer.append(result.getPositionString()).append(",").
                append(String.valueOf(result.getBibNumber())).append(",").
                append(csvEncode(result.getParticipantName())).append(",").
                append(result.getParticipant().getCategory().getShortName()).append(",").
                append(renderDuration(result, DNF_STRING)).
                append(LINE_SEPARATOR);
        }

        protected void printNoResults() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class RelayRaceOverallResultPrinterHTML extends OverallResultPrinterHTML {

        private RelayRaceOverallResultPrinterHTML(final RaceResults race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected List<String> getResultsColumnHeaders() {

            return HEADERS2;
        }

        @Override
        protected List<String> getResultsElements(final RaceResult r) {

            final RelayRaceResult result = (RelayRaceResult) r;
            final NormalisationProcessor processor = race_results.getNormalisationProcessor();
            final Participant participant = result.getParticipant();

            return List.of(
                result.getPositionString(),
                String.valueOf(result.getBibNumber()),
                processor.htmlEncode(participant.getName()),
                processor.htmlEncode(participant.getCategory().getLongName()),
                renderDuration(result, DNF_STRING)
            );
        }
    }

    private static final class DetailedResultPrinterCSV extends ResultPrinter {

        private DetailedResultPrinterCSV(final RaceResults race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {

            final int number_of_legs = ((RelayRaceResults) race_results).getNumberOfLegs();

            writer.append(String.join(",", POS1) + ",");

            for (int leg_number = 1; leg_number <= number_of_legs; leg_number++) {

                writer.append(RUNNER + "s " + leg_number + "," + LEG1 + " " + leg_number + ",");
                if (leg_number < number_of_legs) writer.append(SPLIT + " " + leg_number + ",");
            }

            writer.append(TOTAL).append(LINE_SEPARATOR);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final RelayRaceResult result = (RelayRaceResult) r;

            final String team_name = csvEncode(result.getParticipantName());
            final String category_name = result.getParticipant().getCategory().getLongName();

            writer.append(result.getPositionString()).append(",").
                append(String.valueOf(result.getBibNumber())).append(",").
                append(team_name).append(",").
                append(category_name).append(",");

            final String leg_details = ((RelayRaceResults) race_results).getLegDetails(result).stream().
                map(NormalisationProcessor::csvEncode).
                collect(Collectors.joining(","));

            writer.append(leg_details);
            writer.append(LINE_SEPARATOR);
        }

        protected void printNoResults() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class DetailedResultPrinterHTML extends OverallResultPrinterHTML {

        private DetailedResultPrinterHTML(final RaceResults race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected List<String> getResultsColumnHeaders() {

            final List<String> headers = makeMutableCopy(POS1);
            final int number_of_legs = ((RelayRaceResults) race_results).getNumberOfLegs();

            for (int leg_number = 1; leg_number <= number_of_legs; leg_number++) {

                final String plural = ((RelayRaceResults) race_results).getPairedLegs().get(leg_number - 1) ? "s" : "";

                headers.add(RUNNER + plural + " " + leg_number);
                headers.add(LEG1 + " " + leg_number);
                headers.add(leg_number < number_of_legs ? SPLIT + " " + leg_number : TOTAL);
            }

            return headers;
        }

        @Override
        protected List<String> getResultsElements(final RaceResult r) {

            final RelayRaceResult result = (RelayRaceResult) r;
            final Participant participant = result.getParticipant();
            final NormalisationProcessor processor = race_results.getNormalisationProcessor();
            final List<String> elements = new ArrayList<>();

            elements.add(result.getPositionString());
            elements.add(String.valueOf(result.getBibNumber()));
            elements.add(processor.htmlEncode(participant.getName()));
            elements.add(processor.htmlEncode(participant.getCategory().getLongName()));

            for (final String element : ((RelayRaceResults) race_results).getLegDetails(result))
                elements.add(processor.htmlEncode(element));

            return elements;
        }
    }

    private static final class LegResultPrinterCSV extends ResultPrinter {

        final int leg;

        private LegResultPrinterCSV(final RaceResults race, final OutputStreamWriter writer, final int leg) {

            super(race, writer);
            this.leg = leg;
        }

        @Override
        public void printResultsHeader() throws IOException {

            final String plural = ((RelayRaceResults) race_results).getPairedLegs().get(leg - 1) ? "s" : "";
            writer.append(POS + "," + RUNNER + plural + "," + TIME + LINE_SEPARATOR);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final RelayRaceLegResult result = (RelayRaceLegResult) r;
            final String runner_names = csvEncode(((Team) result.getParticipant()).getRunnerNames().get(result.getLegNumber() - 1));

            writer.append(result.getPositionString()).append(",").
                append(runner_names).append(",").
                append(renderDuration(result, DNF_STRING)).
                append(LINE_SEPARATOR);
        }

        protected void printNoResults() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class LegResultPrinterHTML extends OverallResultPrinterHTML {

        final int leg;

        private LegResultPrinterHTML(final RaceResults race, final OutputStreamWriter writer, final int leg) {

            super(race, writer);
            this.leg = leg;
        }

        @Override
        protected List<String> getResultsColumnHeaders() {

            final List<Boolean> paired_legs = ((RelayRaceResults) race_results).getPairedLegs();
            final String plural = paired_legs.get(leg - 1) ? "s" : "";

            return List.of(
                POS,
                RUNNER + plural,
                TIME);
        }

        @Override
        protected List<String> getResultsElements(final RaceResult r) {

            final NormalisationProcessor processor = race_results.getNormalisationProcessor();
            final RelayRaceLegResult leg_result = (RelayRaceLegResult) r;
            final String runner_names = ((Team) leg_result.getParticipant()).getRunnerNames().get(leg_result.getLegNumber() - 1);

            return List.of(
                leg_result.getPositionString(),
                processor.htmlEncode(runner_names),
                renderDuration(leg_result, DNF_STRING)
            );
        }
    }

    private static final class PrizeResultPrinterPDF extends ResultPrinter {

        private final Document document;

        public PrizeResultPrinterPDF(final RaceResults race, final Document document) {

            super(race, null);
            this.document = document;
        }

        @Override
        public void printResult(final RaceResult result) throws IOException {

            final PdfFont font = getFont(PDF_PRIZE_FONT_NAME);
            final Paragraph paragraph = new Paragraph().setFont(font).setMarginBottom(0);

            paragraph.add(new Text(
                result.getPositionString() + ": " +
                result.getParticipant() + " " +
                renderDuration(result, DNF_STRING)).setFont(font));

            document.add(paragraph);
        }

        @Override
        public void printNoResults() throws IOException {

            document.add(new Paragraph(NO_RESULTS).setFont(getFont(PDF_PRIZE_FONT_ITALIC_NAME)));
        }
    }
}

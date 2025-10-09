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

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.grahamkirby.race_timing.common.*;
import org.grahamkirby.race_timing.relay_race.RelayRaceOutputPDF;
import org.grahamkirby.race_timing.series_race.SeriesRace;
import org.grahamkirby.race_timing.series_race.SeriesRaceOutputText;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.List;

import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.Config.DNF_STRING;
import static org.grahamkirby.race_timing.common.Config.encode;
import static org.grahamkirby.race_timing.common.Normalisation.renderDuration;
import static org.grahamkirby.race_timing.common.RaceOutput.getOutputStream;
import static org.grahamkirby.race_timing.common.RaceOutput.getOutputStreamPath;

public class IndividualRaceOutput {

    private static final String OVERALL_RESULTS_HEADER = "Pos,No,Runner,Club,Category,Time" + LINE_SEPARATOR;

    private final Race race;

    IndividualRaceOutput(final Race race) {
        this.race = race;
    }

    void printResultsHTML() throws IOException {

        RaceOutput.printResults(race, OverallResultPrinterHTML::new);
    }

    /** Prints all details to a single web page. */
    void printCombinedHTML() throws IOException {

        final OutputStream stream = getOutputStream(race, "combined", HTML_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            printPrizesWithHeaderHTML(writer, race, PrizeResultPrinterHTML::new);
            printTeamPrizesHTML(writer, race);
            printResultsWithHeaderHTML(writer, race, OverallResultPrinterHTML::new);
        }
    }

    void printPrizesHTML() throws IOException {

        final OutputStream stream = getOutputStream(race, "prizes", HTML_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(getPrizesHeaderHTML(race));
            RaceOutput.printPrizes(race, writer, PrizeResultPrinterHTML::new);
            printTeamPrizesHTML(writer, race);
        }
    }

    public static void printPrizesWithHeaderHTML(final OutputStreamWriter writer, final Race race, final ResultPrinterGenerator make_prize_result_printer) throws IOException {

        writer.append("<h3>Results</h3>").append(LINE_SEPARATOR);
        writer.append(getPrizesHeaderHTML(race));

        RaceOutput.printPrizes(race, writer, make_prize_result_printer);
    }

    public static void printResultsWithHeaderHTML(final OutputStreamWriter writer, final Race race, final ResultPrinterGenerator make_overall_result_printer) throws IOException {

        writer.append("<h4>Overall</h4>").append(LINE_SEPARATOR);

        RaceOutput.printResults(writer, make_overall_result_printer.apply(race, writer), IndividualRaceOutput::getResultsSubHeaderHTML, race);
        writer.append(SOFTWARE_CREDIT_LINK_TEXT);
    }

    public static String getResultsSubHeaderHTML(final String s) {
        return "<p></p>" + LINE_SEPARATOR + "<h4>" + s + "</h4>" + LINE_SEPARATOR;
    }

    public static String getPrizesHeaderHTML(final Race race) {

        final String header = race.getSpecific() instanceof final SeriesRace series_race && series_race.getNumberOfRacesTakenPlace() < (int) race.getConfig().get(KEY_NUMBER_OF_RACES_IN_SERIES) ? "Current Standings" : "Prizes";
        return "<h4>" + header + "</h4>" + LINE_SEPARATOR;
    }

    private static void printTeamPrizesHTML(final OutputStreamWriter writer, final Race race) throws IOException {

        final List<String> team_prizes = ((IndividualRaceImpl) race.getSpecific()).getTeamPrizes();

        if (!team_prizes.isEmpty()) {

            writer.append("<h4>Team Prizes</h4>").append(LINE_SEPARATOR);
            writer.append("<ul>").append(LINE_SEPARATOR);

            for (final String team_prize : team_prizes)
                writer.append("<li>").append(team_prize).append("</li>").append(LINE_SEPARATOR);

            writer.append("</ul>").append(LINE_SEPARATOR);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OverallResultPrinterHTML extends SingleRaceOutputPrinterHTML {

        private OverallResultPrinterHTML(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected List<String> getResultsColumnHeaders() {

            return List.of("Pos", "No", "Runner", "Club", "Category", "Time");
        }
    }

    private static final class PrizeResultPrinterHTML extends org.grahamkirby.race_timing.common.PrizeResultPrinterHTML {

        private PrizeResultPrinterHTML(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected String renderDetail(final RaceResult result) {
            return ((Runner) result.getParticipant()).club;
        }

        @Override
        protected String renderPerformance(final RaceResult result) {
            return renderDuration((RaceResultWithDuration) result, DNF_STRING);
        }
    }

    void printResultsCSV() throws IOException {

        final OutputStream stream = getOutputStream(race, "overall", CSV_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(OVERALL_RESULTS_HEADER);
            RaceOutput.printResults(writer, new OverallResultPrinterCSV(race, writer), _ -> "", race);
        }
    }

    private static final class OverallResultPrinterCSV extends ResultPrinter {

        // TODO investigate Files.write.
        private OverallResultPrinterCSV(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        public void printResult(final RaceResult r) throws IOException {

            final SingleRaceResult result = (SingleRaceResult) r;
            final Participant participant = result.getParticipant();

            writer.append(result.getPositionString() + "," + result.bib_number + "," + encode(participant.name) + ",");
            writer.append(encode(((Runner) participant).club) + "," + participant.category.getShortName() + "," + renderDuration(((RaceResultWithDuration) result), DNF_STRING) + LINE_SEPARATOR);
        }
    }

    void printPrizesText() throws IOException {

        final OutputStream stream = getOutputStream(race, "prizes", TEXT_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(SeriesRaceOutputText.getPrizesHeader(race));
            SeriesRaceOutputText.printPrizes(writer, race);
            printTeamPrizesText(writer);
        }
    }

    /** Prints out the words converted to title case, and any other processing notes. */
    void printNotesText() throws IOException {

        SeriesRaceOutputText.printNotes(race);
    }

    private void printTeamPrizesText(final OutputStreamWriter writer) throws IOException {

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

    public static final class PrizeResultPrinterText extends ResultPrinterText {

        public PrizeResultPrinterText(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult result) throws IOException {

            writer.append(result.getPositionString() + ": " + result.getParticipantName() + " " + result.getPrizeDetailText() + LINE_SEPARATOR);
        }
    }

    void printPrizesPDF() throws IOException {

        final Path path = getOutputStreamPath(race, "prizes", PDF_FILE_SUFFIX);
        final PdfWriter writer = new PdfWriter(path.toString());

        try (final Document document = new Document(new PdfDocument(writer))) {

            RelayRaceOutputPDF.printPrizes(race, document);

            final List<String> team_prizes = ((IndividualRaceImpl)race.getSpecific()).getTeamPrizes();

            if (!team_prizes.isEmpty()) {
                document.add(new Paragraph("Team Prizes").
                    setFont(getFont(PDF_PRIZE_FONT_BOLD_NAME)).
                    setUnderline().
                    setPaddingTop(PDF_PRIZE_FONT_SIZE));

                for (final String team_prize : team_prizes)
                    document.add(new Paragraph(team_prize));
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static PdfFont getFont(final String font_name) throws IOException {
        return PdfFontFactory.createFont(font_name);
    }
}

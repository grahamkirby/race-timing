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

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.grahamkirby.race_timing.common.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.List;

import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.Normalisation.renderDuration;

@SuppressWarnings("preview")
public class IndividualRaceOutput extends RaceOutput {

    private static final String OVERALL_RESULTS_HEADER = "Pos,No,Runner,Club,Category,Time" + LINE_SEPARATOR;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void outputResults() throws IOException {

        printOverallResults();

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

    private void printPrizes() throws IOException {

        printPrizesPDF();
        printPrizesHTML();
        printPrizesText();
    }

    private void printCombined() throws IOException {

        printCombinedHTML();
    }

    private void printResultsHTML() throws IOException {

        printResultsHTML(IndividualRaceOverallResultPrinterHTML::new);
    }

    /** Prints all details to a single web page. */
    private void printCombinedHTML() throws IOException {

        final OutputStream stream = getOutputStream("combined", HTML_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            printPrizesWithHeaderHTML(writer, IndividualRacePrizeResultPrinterHTML::new);
            printTeamPrizesHTML(writer, race);
            printResultsWithHeaderHTML(writer, IndividualRaceOverallResultPrinterHTML::new);
        }
    }

    private void printPrizesHTML() throws IOException {

        final OutputStream stream = getOutputStream("prizes", HTML_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(getPrizesHeaderHTML());
            printPrizesHTML(writer, IndividualRacePrizeResultPrinterHTML::new);
            printTeamPrizesHTML(writer, race);
        }
    }

    private void printTeamPrizesHTML(final OutputStreamWriter writer, final Race race1) throws IOException {

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

    private static final class IndividualRaceOverallResultPrinterHTML extends OverallResultPrinterHTML {

        private IndividualRaceOverallResultPrinterHTML(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected List<String> getResultsColumnHeaders() {

            return List.of("Pos", "No", "Runner", "Club", "Category", "Time");
        }
    }

    private static final class IndividualRacePrizeResultPrinterHTML extends PrizeResultPrinterHTML {

        private IndividualRacePrizeResultPrinterHTML(final Race race, final OutputStreamWriter writer) {
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

    private void printResultsCSV() throws IOException {

        final OutputStream stream = getOutputStream("overall", CSV_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(OVERALL_RESULTS_HEADER);
            printResults(writer, new OverallResultPrinterCSV(race, writer), _ -> "");
        }
    }

//    protected String getResultsSubHeaderHTML(final String s) {
//        return "";
//    }

    private static final class OverallResultPrinterCSV extends ResultPrinter {

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

    protected void printPrizesText() throws IOException {

        final OutputStream stream = getOutputStream("prizes", TEXT_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            printPrizesHeaderText(writer);
            printPrizesText(writer);
            printTeamPrizesText(writer);
        }
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

    protected void printPrizesPDF() throws IOException {

        final Path path = getOutputStreamPath("prizes", PDF_FILE_SUFFIX);
        final PdfWriter writer = new PdfWriter(path.toString());

        try (final Document document = new Document(new PdfDocument(writer))) {

            printPrizesPDF(document);
            printTeamPrizesPDF(document);
        }
    }

    private void printTeamPrizesPDF(final Document document) throws IOException {

        final List<String> team_prizes = ((IndividualRaceImpl) race.getSpecific()).getTeamPrizes();

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

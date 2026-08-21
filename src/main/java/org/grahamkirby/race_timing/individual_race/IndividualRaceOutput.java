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
package org.grahamkirby.race_timing.individual_race;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import org.grahamkirby.race_timing.common.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.NormalisationProcessor.csvEncode;
import static org.grahamkirby.race_timing.common.NormalisationProcessor.renderDuration;
import static org.grahamkirby.race_timing.individual_race.IndividualRaceResults.*;
import static org.grahamkirby.race_timing.individual_race.IndividualRaceResultsProcessor.getAggregatePosition;

public class IndividualRaceOutput extends RaceOutput {

    public IndividualRaceOutput(final Config config) {
        super(config);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected ResultPrinterGenerator getOverallResultCSVPrinterGenerator() {
        return IndividualRaceOverallResultPrinterCSV::new;
    }

    @Override
    protected ResultPrinterGenerator getOverallResultHTMLPrinterGenerator() {
        return IndividualRaceOverallResultPrinterHTML::new;
    }

    @Override
    protected ResultPrinterGenerator getPrizeHTMLPrinterGenerator() {
        return IndividualPrizeResultPrinterHTML::new;
    }

    @Override
    protected BiFunction<RaceResults, Document, ResultPrinter> getPrizePDFPrinterGenerator() {
        return PrizeResultPrinterPDF::new;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void printPrizesHTML() throws IOException {

        final OutputStream stream = getOutputStream(PRIZES.toLowerCase(), HTML_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(getPrizesHeaderHTML());
            printPrizesHTML(writer, getPrizeHTMLPrinterGenerator().apply(race_results, writer));
            printTeamPrizesHTML(writer);
        }
    }

    @Override
    protected void printPrizesPDF() throws IOException {

        final Path path = getOutputStreamPath(PRIZES.toLowerCase(), PDF_FILE_SUFFIX);
        final PdfWriter writer = new PdfWriter(path.toString());

        try (final Document document = new Document(new PdfDocument(writer))) {

            printPrizesPDF(document);
            printTeamPrizesPDF(document);
        }
    }

    @Override
    protected void printPrizesText() throws IOException {

        final OutputStream stream = getOutputStream(PRIZES.toLowerCase(), TEXT_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            printPrizesHeaderText(writer);
            printPrizesText(writer, new PrizeResultPrinterText(race_results, writer));
            printTeamPrizesText(writer);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void printTeamPrizesHTML(final OutputStreamWriter writer) throws IOException {

        final List<TeamPerformance> team_prizes = ((IndividualRaceResults) race_results).getTeamPrizes();

        if (!team_prizes.isEmpty()) {

            writer.append("<h4>" + TEAM_PRIZES + "</h4>").append(LINE_SEPARATOR);
            writer.append("<ul>").append(LINE_SEPARATOR);

            for (final TeamPerformance team_performance : team_prizes) {

                final int best_team_total = getAggregatePosition(team_performance);

                writer.append("    <li>").
                    append(FIRST + " <strong>").
                    append(team_performance.gender().toLowerCase()).
                    append(" " + TEAM.toLowerCase() + "</strong>: ").
                    append(team_performance.club()).
                    append(" (").append(String.valueOf(best_team_total)).append("):").append(LINE_SEPARATOR).
                    append("        <ul>").append(LINE_SEPARATOR).
                    append("            <li>").
                    append(
                        team_performance.runner_performances().stream().
                            map(runner_performance -> runner_performance.name() + " (" + runner_performance.position() + ")").
                            collect(Collectors.joining(", "))).
                    append("</li>").append(LINE_SEPARATOR).
                    append("        </ul>").append(LINE_SEPARATOR).
                    append("    </li>").append(LINE_SEPARATOR).
                    append("    <br />").append(LINE_SEPARATOR);
            }

            writer.append("</ul>").append(LINE_SEPARATOR);
        }
    }

    private void printTeamPrizesPDF(final Document document) throws IOException {

        final PdfFont bold_font = getFont(PDF_PRIZE_FONT_BOLD_NAME);

        final List<TeamPerformance> team_prizes = ((IndividualRaceResults) race_results).getTeamPrizes();

        if (!team_prizes.isEmpty()) {

            document.add(new Paragraph(TEAM_PRIZES).
                setFont(getFont(PDF_PRIZE_FONT_BOLD_NAME)).
                setUnderline().
                setPaddingTop(PDF_PRIZE_FONT_SIZE));

            for (final TeamPerformance team_performance : team_prizes) {

                final int best_team_total = getAggregatePosition(team_performance);

                final Paragraph paragraph1 = new Paragraph();
                paragraph1.add(new Text(FIRST + " "));
                paragraph1.add(new Text(team_performance.gender().toLowerCase() + " " + TEAM.toLowerCase()).setFont(bold_font));
                paragraph1.add(new Text(": " + team_performance.club() + " (" + best_team_total + "):"));

                final Paragraph paragraph2 = new Paragraph().setFirstLineIndent(INDENT);
                paragraph2.add(new Text(team_performance.runner_performances().stream().
                    map(runner_performance -> runner_performance.name() + " (" + runner_performance.position() + ")").
                    collect(Collectors.joining(", "))));

                document.add(paragraph1);
                document.add(paragraph2);
            }
        }
    }

    private void printTeamPrizesText(final OutputStreamWriter writer) throws IOException {

        final List<TeamPerformance> team_prizes = ((IndividualRaceResults) race_results).getTeamPrizes();

        if (!team_prizes.isEmpty()) {

            writer.append(TEAM_PRIZES).append(LINE_SEPARATOR);
            writer.append(UNDERLINE).append(LINE_SEPARATOR).append(LINE_SEPARATOR);

            for (final TeamPerformance team_performance : team_prizes) {

                final int best_team_total = getAggregatePosition(team_performance);

                writer.append(FIRST + " " + team_performance.gender().toLowerCase() + " " + TEAM.toLowerCase() + ": " + team_performance.club() + " (" + best_team_total + "):" + LINE_SEPARATOR + "   " +
                    team_performance.runner_performances().stream().
                        map(runner_performance -> runner_performance.name() + " (" + runner_performance.position() + ")").
                        collect(Collectors.joining(", ")));
                writer.append(LINE_SEPARATOR).append(LINE_SEPARATOR);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Prints all details to a single web page. */
    @Override
    protected void printCombinedHTML() throws IOException {

        final OutputStream stream = getOutputStream(COMBINED, HTML_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            printPrizesWithHeaderHTML(writer, PrizeResultPrinterHTML::new);
            printTeamPrizesHTML(writer);
            printResultsWithHeaderHTML(writer, IndividualRaceOverallResultPrinterHTML::new);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class IndividualRaceOverallResultPrinterCSV extends ResultPrinter {

        private IndividualRaceOverallResultPrinterCSV(final RaceResults race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {

            writer.append(String.join(CSV_SEPARATOR, HEADERS)).append(LINE_SEPARATOR);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final SingleRaceResult result = (SingleRaceResult) r;
            final Participant participant = result.getParticipant();

            writer.append(result.getPositionString()).append(",").
                append(String.valueOf(result.getBibNumber())).append(",").
                append(csvEncode(participant.getName())).append(",").
                append(csvEncode(((Runner) participant).getClub())).append(",").
                append(participant.getCategory().getShortName()).append(",").
                append(renderDuration(result, DNF_STRING)).
                append(LINE_SEPARATOR);
        }

        protected void printNoResults() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class IndividualRaceOverallResultPrinterHTML extends OverallResultPrinterHTML {

        private IndividualRaceOverallResultPrinterHTML(final RaceResults race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected List<String> getResultsColumnHeaders() {

            return HEADERS;
        }

        @Override
        protected List<String> getResultsElements(final RaceResult r) {

            final SingleRaceResult result = (SingleRaceResult) r;
            final Participant participant = result.getParticipant();
            final NormalisationProcessor normalisation = race_results.getNormalisationProcessor();

            return List.of(
                result.getPositionString(),
                String.valueOf(result.getBibNumber()),
                normalisation.htmlEncode(participant.getName()),
                normalisation.htmlEncode(((Runner) participant).getClub()),
                normalisation.htmlEncode(participant.getCategory().getShortName()),
                renderDuration(result, DNF_STRING)
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

    public static class IndividualPrizeResultPrinterHTML extends ResultPrinter {

        public IndividualPrizeResultPrinterHTML(final RaceResults race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        //////////////////////////////////////////////////////////////////////////////////////////////////

        @Override
        public void printResultsHeader() throws IOException {

            writer.append("<ul>").append(LINE_SEPARATOR);
        }

        @Override
        public void printResult(final RaceResult result) throws IOException {

            final NormalisationProcessor normalisation = race_results.getNormalisationProcessor();

            writer.append(
                "    <li>" +
                    result.getPositionString() + " " +
                    normalisation.htmlEncode(result.getParticipant().toString()) + " " +
                    renderDuration(result, DNF_STRING) +
                    "</li>" +
                    LINE_SEPARATOR);
        }

        @Override
        public void printResultsFooter() throws IOException {

            writer.append("</ul>").append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        }

        @Override
        public void printNoResults() throws IOException {

            writer.append("<p>" + NO_RESULTS + "</p>").append(LINE_SEPARATOR);
        }
    }
}

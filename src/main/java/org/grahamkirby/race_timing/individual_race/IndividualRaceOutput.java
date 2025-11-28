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
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.Normalisation.renderDuration;
import static org.grahamkirby.race_timing.individual_race.IndividualRaceResults.*;
import static org.grahamkirby.race_timing.individual_race.IndividualRaceResultsCalculator.getAggregatePosition;

@SuppressWarnings("preview")
public class IndividualRaceOutput extends RaceOutput {

    private static final String OVERALL_RESULTS_HEADER = "Pos,No,Runner,Club,Category,Time" + LINE_SEPARATOR;

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
        return IndividualRacePrizeResultPrinterHTML::new;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void printPrizesHTML() throws IOException {

        final OutputStream stream = getOutputStream("prizes", HTML_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(getPrizesHeaderHTML());
            printPrizesHTML(writer, getPrizeHTMLPrinterGenerator().apply(race_results, writer));
            printTeamPrizesHTML(writer);
        }
    }

    @Override
    protected void printPrizesPDF() throws IOException {

        final Path path = getOutputStreamPath("prizes", PDF_FILE_SUFFIX);
        final PdfWriter writer = new PdfWriter(path.toString());

        try (final Document document = new Document(new PdfDocument(writer))) {

            printPrizesPDF(document);
            printTeamPrizesPDF(document);
        }
    }

    @Override
    protected void printPrizesText() throws IOException {

        final OutputStream stream = getOutputStream("prizes", TEXT_FILE_SUFFIX);

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

            writer.append("<h4>Team Prizes</h4>").append(LINE_SEPARATOR);
            writer.append("<ul>").append(LINE_SEPARATOR);

            for (final TeamPerformance team_performance : team_prizes) {

                final int best_team_total = getAggregatePosition(team_performance);

                writer.append("    <li>").
                    append("First <strong>").
                    append(team_performance.gender().toLowerCase()).
                    append(" team</strong>: ").
                    append(team_performance.club()).
                    append(" (").append(String.valueOf(best_team_total)).append("):").append(LINE_SEPARATOR).
                    append("        <ul>").append(LINE_SEPARATOR).
                    append("            <li>").
                    append(
                        team_performance.runner_performances().stream().
                            map(runnerPerformance -> runnerPerformance.name() + " (" + runnerPerformance.position() + ")").
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

            document.add(new Paragraph("Team Prizes").
                setFont(getFont(PDF_PRIZE_FONT_BOLD_NAME)).
                setUnderline().
                setPaddingTop(PDF_PRIZE_FONT_SIZE));

            for (final TeamPerformance team_performance : team_prizes) {

                final int best_team_total = getAggregatePosition(team_performance);

                final Paragraph paragraph1 = new Paragraph();
                paragraph1.add(new Text("First "));
                paragraph1.add(new Text(team_performance.gender().toLowerCase() + " team").setFont(bold_font));
                paragraph1.add(new Text(": " + team_performance.club() + " (" + best_team_total + "):"));

                final Paragraph paragraph2 = new Paragraph().setFirstLineIndent(24);
                paragraph2.add(new Text(team_performance.runner_performances().stream().
                    map(performance -> performance.name() + " (" + performance.position() + ")").
                    collect(Collectors.joining(", "))));

                document.add(paragraph1);
                document.add(paragraph2);
            }
        }
    }

    private void printTeamPrizesText(final OutputStreamWriter writer) throws IOException {

        final List<TeamPerformance> team_prizes = ((IndividualRaceResults) race_results).getTeamPrizes();

        if (!team_prizes.isEmpty()) {

            writer.append("Team Prizes\n");
            writer.append("-----------\n\n");

            for (final TeamPerformance team_performance : team_prizes) {

                final int best_team_total = getAggregatePosition(team_performance);

                writer.append("First " + team_performance.gender().toLowerCase() + " team: " + team_performance.club() + " (" + best_team_total + "):" + LINE_SEPARATOR + "   " +
                    team_performance.runner_performances().stream().
                        map(runnerPerformance -> runnerPerformance.name() + " (" + runnerPerformance.position() + ")").
                        collect(Collectors.joining(", ")));
                writer.append(LINE_SEPARATOR).append(LINE_SEPARATOR);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Prints all details to a single web page. */
    @Override
    protected void printCombinedHTML() throws IOException {

        final OutputStream stream = getOutputStream("combined", HTML_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            printPrizesWithHeaderHTML(writer, IndividualRacePrizeResultPrinterHTML::new);
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

            writer.append(OVERALL_RESULTS_HEADER);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final SingleRaceResult result = (SingleRaceResult) r;
            final Participant participant = result.getParticipant();

            writer.append(result.getPositionString()).append(",").
                append(String.valueOf(result.getBibNumber())).append(",").
                append(encode(participant.getName())).append(",").
                append(encode(((Runner) participant).getClub())).append(",").
                append(participant.getCategory().getShortName()).append(",").
                append(renderDuration(result, DNF_STRING)).
                append(LINE_SEPARATOR);
        }
    }

    private static final class IndividualRaceOverallResultPrinterHTML extends OverallResultPrinterHTML {

        private IndividualRaceOverallResultPrinterHTML(final RaceResults race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected List<String> getResultsColumnHeaders() {

            return List.of("Pos", "No", "Runner", "Club", "Category", "Time");
        }

        @Override
        protected List<String> getResultsElements(final RaceResult r) {

            final SingleRaceResult result = (SingleRaceResult) r;

            return List.of(
                result.getPositionString(),
                String.valueOf(result.getBibNumber()),
                race_results.getNormalisation().htmlEncode(result.getParticipant().getName()),
                ((Runner) result.getParticipant()).getClub(),
                result.getParticipant().getCategory().getShortName(),
                renderDuration(result, DNF_STRING)
            );
        }
    }

    private static final class IndividualRacePrizeResultPrinterHTML extends PrizeResultPrinterHTML {

        private IndividualRacePrizeResultPrinterHTML(final RaceResults race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected String renderDetail(final RaceResult result) {
            return ((Runner) result.getParticipant()).getClub();
        }

        @Override
        protected String renderPerformance(final RaceResult result) {
            return renderDuration(result, DNF_STRING);
        }
    }
}

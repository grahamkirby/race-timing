package series_race.fife_ac_midweek;

import common.Race;
import common.RaceResult;
import common.categories.Category;
import common.output.RaceOutputHTML;
import individual_race.IndividualRace;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;

public class MidweekRaceOutputHTML extends RaceOutputHTML {

    public MidweekRaceOutputHTML(final Race race) {
        super(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void printCombined() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve("combined.html"));

        try (final OutputStreamWriter html_writer = new OutputStreamWriter(stream)) {

            html_writer.append("""
                    <h3><strong>Results</strong></h3>
                    """);

            printPrizes(html_writer);

            html_writer.append("""
                    <h4>Overall</h4>
                    """);

            printOverallResults(html_writer);
        }
    }

    @Override
    protected void printPrizes(final OutputStreamWriter writer, final Category category) throws IOException {

        final List<RaceResult> category_prize_winners = race.prize_winners.get(category);

        writer.append("<p><strong>").
                append(category.getShortName()).
                append("</strong></p>\n").
                append("<ul>\n");

        setPositionStrings(category_prize_winners, true);
        printResults(category_prize_winners, new PrizeResultPrinterHTML(((MidweekRace)race), writer));

        writer.append("</ul>\n\n");
    }

    @Override
    protected void printOverallResults(final OutputStreamWriter writer) throws IOException {

        printOverallResultsHeader(writer);
        printOverallResultsBody(writer);
        printOverallResultsFooter(writer);
    }

    @Override
    protected void printOverallResultsHeader(final OutputStreamWriter writer) throws IOException {

        writer.append("""
                <table class="fac-table">
                               <thead>
                                   <tr>
                                       <th>Pos</th>
                                       <th>Runner</th>
                                       <th>Category</th>
                                       <th>Club</th>
            """);

        final List<IndividualRace> races = ((MidweekRace)race).getRaces();

        for (int i = 0; i < races.size(); i++) {
            if (races.get(i) != null) {
                writer.append("<th>Race ").
                        append(String.valueOf(i + 1)).
                        append("</th>\n");
            }
        }

        writer.append("""
                                       <th>Total</th>
                                       <th>Completed</th>
                                   </tr>
                               </thead>
                               <tbody>
            """);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void printOverallResultsBody(final OutputStreamWriter writer) throws IOException {

        final List<RaceResult> overall_results = race.getOverallResults();

        setPositionStrings(overall_results, true);

        for (final RaceResult res : overall_results) {

            MidweekRaceResult result = ((MidweekRaceResult)res);

            writer.append("""
                        <tr>
                        <td>""").
                    append(result.shouldDisplayPosition() ? result.position_string : "").
                    append("""
                            </td>
                            <td>""").
                    append(htmlEncode(result.runner.name)).append("""
                            </td>
                            <td>""").
                    append(result.runner.category.getShortName()).append("""
                            </td>
                            <td>""").
                    append(result.runner.club).append("""
                            </td>
                            """);

            for (int i = 0; i < result.scores.size(); i++)
                if (result.scores.get(i) >= 0)
                    writer.append("<td>").append(String.valueOf(result.scores.get(i))).append("</td>\n");

            writer.append("""
                            <td>""").
                    append(String.valueOf(result.totalScore())).
                    append("""
                            </td>
                            <td>""").
                    append(result.completed() ? "Y" : "N").
                    append("""
                        </td>
                        </tr>
                        """);
        }
    }

    private void printOverallResultsFooter(final OutputStreamWriter writer) throws IOException {

        writer.append("""
                </tbody>
            </table>
            """);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private record PrizeResultPrinterHTML(MidweekRace race, OutputStreamWriter writer) implements ResultPrinter {

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final MidweekRaceResult result = ((MidweekRaceResult)r);

            writer.append("<li>").
                    append(result.position_string).append(": ").
                    append(result.runner.name).
                    append(" (").
                    append(result.runner.category.getShortName()).
                    append(") ").
                    append(String.valueOf(result.totalScore())).
                    append("</li>\n");
        }

        @Override
        public void printNoResults() throws IOException {

            writer.append("No results\n");
        }
    }
}

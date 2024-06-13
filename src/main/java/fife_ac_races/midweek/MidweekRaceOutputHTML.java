package fife_ac_races.midweek;

import common.categories.Category;
import common.Race;
import common.output.RaceOutputHTML;
import common.RaceResult;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;

public class MidweekRaceOutputHTML extends RaceOutputHTML {

    public MidweekRaceOutputHTML(final Race race) {
        super(race);
    }

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

    public void printPrizes(final OutputStreamWriter writer, final Category category) throws IOException {

        final List<RaceResult> category_prize_winners = race.prize_winners.get(category);

        writer.append("<p><strong>").
                append(category.getShortName()).
                append("</strong></p>\n").
                append("<ol>\n");

        printResults(category_prize_winners, new PrizeResultPrinterHTML(((MidweekRace)race), writer));

        writer.append("</ol>\n\n");
    }

    private record PrizeResultPrinterHTML(MidweekRace race, OutputStreamWriter writer) implements ResultPrinter {

        @Override
        public void printResult(RaceResult r) throws IOException {

            final MidweekRaceResult result = ((MidweekRaceResult)r);

            writer.append("<li>").
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

    @Override
    protected void printOverallResults(OutputStreamWriter writer) throws IOException {

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

        for (int i = 0; i < ((MidweekRace)race).races.size(); i++) {
            if (((MidweekRace)race).races.get(i) != null) {
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

    private void printOverallResultsBody(final OutputStreamWriter writer) throws IOException {

        for (final RaceResult res : race.getOverallResults()) {

            MidweekRaceResult result = ((MidweekRaceResult)res);

            writer.append("""
                        <tr>
                        <td>""").
                    append(result.position_string).
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
}

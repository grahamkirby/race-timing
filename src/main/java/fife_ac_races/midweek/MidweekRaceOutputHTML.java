package fife_ac_races.midweek;

import common.Category;
import common.Race;
import common.RaceOutput;
import common.Runner;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;

public class MidweekRaceOutputHTML extends RaceOutput {

    public MidweekRaceOutputHTML(final Race race) {
        super(race);
    }

    @Override
    public void printPrizes() throws IOException {

        printPrizesHTML();
    }

    @Override
    public void printOverallResults() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(overall_results_filename + ".html"));

        try (final OutputStreamWriter html_writer = new OutputStreamWriter(stream)) {
            printOverallResults(html_writer);
        }
    }

    public void printCombined() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve("combined.html"));

        try (final OutputStreamWriter html_writer = new OutputStreamWriter(stream)) {

            html_writer.append("""
                    <h3><strong>Results</strong></h3>
                    """);

            printPrizesHTML(html_writer);

            html_writer.append("""
                    <h4>Overall</h4>
                    """);

            printOverallResults(html_writer);
        }
    }

    public void printPrizes(final Category category, final OutputStreamWriter writer) throws IOException {

        final List<Runner> category_prize_winners = ((MidweekRace)race).prize_winners.get(category);

        if (category_prize_winners != null) {
            writer.append("<p><strong>").
                    append(category.getShortName()).
                    append("</strong></p>\n").
                    append("<ol>\n");

            if (category_prize_winners.isEmpty())
                writer.append("No results\n");

            for (final Runner entry : category_prize_winners) {

                int indexOfRunner = ((MidweekRace)race).findIndexOfRunner(entry);
                MidweekRaceResult overallResult = ((MidweekRace)race).getOverallResults()[indexOfRunner];
                int score = overallResult.totalScore();

                writer.append("<li>").
                        append(entry.name).
                        append(" (").
                        append(entry.category.getShortName()).
                        append(") ").
                        append(String.valueOf(score)).
                        append("</li>\n");
            }

            writer.append("</ol>\n\n");
        }
    }

    @Override
    protected void printOverallResults(OutputStreamWriter html_writer) throws IOException {

        printOverallResultsHeader(html_writer);
        printOverallResultsBody(html_writer);
        printOverallResultsFooter(html_writer);
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

        for (int i = 0; i < ((MidweekRace)race).races.length; i++) {
            if (((MidweekRace)race).races[i] != null) {
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

        for (final MidweekRaceResult result : ((MidweekRace)race).getOverallResults()) {

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

            for (int i = 0; i < result.scores.length; i++)
                if (result.scores[i] >= 0)
                    writer.append("<td>").append(String.valueOf(result.scores[i])).append("</td>\n");

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

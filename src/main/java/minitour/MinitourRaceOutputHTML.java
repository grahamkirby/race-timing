package minitour;

import common.Category;
import individual_race.Runner;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;

public class MinitourRaceOutputHTML extends MinitourRaceOutput {

    public MinitourRaceOutputHTML(final MinitourRace race) {
        super(race);
    }

    @Override
    public void printPrizes() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(prizes_filename + ".html"));

        try (final OutputStreamWriter html_writer = new OutputStreamWriter(stream)) {
            printPrizes(html_writer);
        }
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

            printPrizes(html_writer);

            html_writer.append("""
                    <h4>Overall</h4>
                    """);

            printOverallResults(html_writer);
        }
    }

    private void printPrizes(OutputStreamWriter html_writer) throws IOException {

        html_writer.append("<h4>Prizes</h4>\n");

        for (final Category category : race.categories.getCategoriesInReportOrder())
            printPrizes(category, html_writer);
    }

    private void printPrizes(final Category category, final OutputStreamWriter writer) throws IOException {

        final List<Runner> category_prize_winners = race.prize_winners.get(category);

        if (category_prize_winners != null) {
            writer.append("<p><strong>").append(category.getShortName()).append("</strong></p>\n");
            writer.append("<ol>\n");

            if (category_prize_winners.isEmpty())
                writer.append("No results\n");

            for (final Runner entry : category_prize_winners) {

                int indexOfRunner = race.findIndexOfRunner(entry);
                MinitourRaceResult overallResult = race.getOverallResults()[indexOfRunner];
                Duration time = overallResult.duration();

                writer.append("<li>").
                        append(entry.name).append(" (").
                        append(entry.category.getShortName()).append(") ").
                        append(String.valueOf(time)).append("</li>\n");
            }

            writer.append("</ol>\n\n");
        }
    }

    private void printOverallResults(OutputStreamWriter html_writer) throws IOException {

        printOverallResultsHeader(html_writer);
        printOverallResultsBody(html_writer);
        printOverallResultsFooter(html_writer);
    }

    private void printOverallResultsHeader(final OutputStreamWriter writer) throws IOException {

        writer.append("""
                <table class="fac-table">
                               <thead>
                                   <tr>
                                       <th>Pos</th>
                                       <th>Runner</th>
                                       <th>Category</th>
                                       <th>Club</th>
                                       <th>Score</th>
                                   </tr>
                               </thead>
                               <tbody>
            """);
    }

    private void printOverallResultsBody(final OutputStreamWriter writer) throws IOException {

        int position = 1;

        for (final MinitourRaceResult result : race.getOverallResults()) {

            writer.append("""
                        <tr>
                            <td>""");
            writer.append(String.valueOf(position++));
            writer.append("""
                            </td>
                            <td>""");
            writer.append(htmlEncode(result.runner.name));
            writer.append("""
                            </td>
                            <td>""");
            writer.append(result.runner.category.getShortName());
            writer.append("""
                            </td>
                            <td>""");
            writer.append(result.runner.club);
            writer.append("""
                            </td>
                            <td>""");
            writer.append(String.valueOf(result.duration()));
            writer.append("""
                            </td>
                        </tr>""");
        }
    }

    private void printOverallResultsFooter(final OutputStreamWriter writer) throws IOException {

        writer.append("""
                </tbody>
            </table>
            """);
    }

    private String htmlEncode(String s) {

        return s.replaceAll("è", "&egrave;").
                replaceAll("é", "&eacute;").
                replaceAll("ü", "&uuml;").
                replaceAll("’", "&acute;");
    }
}

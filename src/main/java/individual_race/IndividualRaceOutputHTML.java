package individual_race;

import common.Category;
import lap_race.LapRace;
import lap_race.LegResult;
import lap_race.Team;
import lap_race.TeamResult;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;

public class IndividualRaceOutputHTML extends IndividualRaceOutput {

    public IndividualRaceOutputHTML(final IndividualRace results) {
        super(results);
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

        for (final Category category : IndividualRaceCategory.getCategoriesInReportOrder())
            printPrizes(category, html_writer);
    }

    private void printPrizes(final Category category, final OutputStreamWriter writer) throws IOException {

        writer.append("<p><strong>").append(category.shortName()).append("</strong></p>\n");
        writer.append("<ol>\n");

        final List<Runner> category_prize_winners = race.prize_winners.get(category);

        if (category_prize_winners.isEmpty())
            writer.append("No results\n");

        int position = 1;
        for (final Runner runner : category_prize_winners) {

            final Result result = race.overall_results[race.findIndexOfRunnerWithBibNumber(runner.bib_number)];

            writer.append("<li>").
                    append(result.runner.name).append(" (").
                    append(result.runner.category.shortName()).append(") ").
                    append(format(result.duration())).append("</li>\n");
        }

        writer.append("</ol>\n\n");
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
                                       <th>No</th>
                                       <th>Team</th>
                                       <th>Category</th>
                                       <th>Total</th>
                                   </tr>
                               </thead>
                               <tbody>
            """);
    }

    private void printOverallResultsBody(final OutputStreamWriter writer) throws IOException {

        int position = 1;

        for (final Result result : race.overall_results) {

            writer.append("""
                        <tr>
                            <td>""");
            if (!result.dnf()) writer.append(String.valueOf(position++));
            writer.append("""
                            </td>
                            <td>""");
            writer.append(String.valueOf(result.runner.bib_number));
            writer.append("""
                            </td>
                            <td>""");
            writer.append(htmlEncode(result.runner.name));
            writer.append("""
                            </td>
                            <td>""");
            writer.append(result.runner.category.shortName());
            writer.append("""
                            </td>
                            <td>""");
            writer.append(result.dnf() ? DNF_STRING : format(result.duration()));
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

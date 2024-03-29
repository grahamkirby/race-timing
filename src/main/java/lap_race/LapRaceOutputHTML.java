package lap_race;

import common.Category;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;

public class LapRaceOutputHTML extends LapRaceOutput {

    public LapRaceOutputHTML(final LapRace results) {
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

    @Override
    public void printDetailedResults() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(detailed_results_filename + ".html"));

        try (final OutputStreamWriter html_writer = new OutputStreamWriter(stream)) {
            printDetailedResults(html_writer);
        }
    }

    public void printLegResults(final int leg) throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(race_name_for_filenames + "_leg_" + leg + "_" + year + ".html"));

        try (final OutputStreamWriter html_writer = new OutputStreamWriter(stream)) {
            printLegResults(html_writer, leg);
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

            html_writer.append("""
                    <h4>Full Results</h4>
                    """);

            printDetailedResults(html_writer);

            html_writer.append("""
                    <p>M3: mass start leg 3<br />M4: mass start leg 4</p>
                    """);

            for (int leg_number = 1; leg_number <= race.number_of_legs; leg_number++) {

                html_writer.append("<p></p>\n<h4>Leg ").append(String.valueOf(leg_number)).append(" Results</h4>\n");
                printLegResults(html_writer, leg_number);
            }
        }
    }

    private void printPrizes(OutputStreamWriter html_writer) throws IOException {

        html_writer.append("<h4>Prizes</h4>\n");

        for (final Category category : LapRaceCategory.getCategoriesInReportOrder())
            printPrizes(category, html_writer);
    }

    private void printPrizes(final Category category, final OutputStreamWriter writer) throws IOException {

        writer.append("<p><strong>").append(category.shortName()).append("</strong></p>\n");
        writer.append("<ol>\n");

        final List<Team> category_prize_winners = race.prize_winners.get(category);

        if (category_prize_winners.isEmpty())
            writer.append("No results\n");

        for (final Team team : category_prize_winners) {

            final LapRaceResult result = race.overall_results[race.findIndexOfTeamWithBibNumber(team.bib_number)];

            writer.append("<li>").
                    append(result.team.name).append(" (").
                    append(result.team.category.shortName()).append(") ").
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

        for (final LapRaceResult result : race.overall_results) {

            writer.append("""
                        <tr>
                            <td>""");
            if (!result.dnf()) writer.append(String.valueOf(position++));
            writer.append("""
                            </td>
                            <td>""");
            writer.append(String.valueOf(result.team.bib_number));
            writer.append("""
                            </td>
                            <td>""");
            writer.append(htmlEncode(result.team.name));
            writer.append("""
                            </td>
                            <td>""");
            writer.append(result.team.category.shortName());
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

    private void printDetailedResults(OutputStreamWriter html_writer) throws IOException {

        printDetailedResultsHeader(html_writer);
        printDetailedResultsBody(html_writer);
        printDetailedResultsFooter(html_writer);
    }

    private void printDetailedResultsHeader(final OutputStreamWriter writer) throws IOException {

        writer.append("""
                <table class="fac-table">
                               <thead>
                                   <tr>
                                       <th>Pos</th>
                                       <th>No</th>
                                       <th>Team</th>
                                       <th>Category</th>
            """);

        for (int leg_number = 1; leg_number <= race.number_of_legs; leg_number++) {

            writer.append("<th>Runner");
            if (race.paired_legs[leg_number-1]) writer.append("s");
            writer.append(" ").append(String.valueOf(leg_number)).append("</th>");

            writer.append("<th>Leg ").append(String.valueOf(leg_number)).append("</th>");

            if (leg_number < race.number_of_legs)
                writer.append("<th>Split ").append(String.valueOf(leg_number)).append("</th>");
            else
                writer.append("<th>Total</th>");
        }

        writer.append("""
                                   </tr>
                               </thead>
                               <tbody>
            """);
    }

    private void printDetailedResultsBody(final OutputStreamWriter writer) throws IOException {

        for (int result_index = 0; result_index < race.overall_results.length; result_index++)
            printDetailedResult(writer, result_index);
    }

    private void printDetailedResult(final OutputStreamWriter writer, final int result_index) throws IOException {

        final LapRaceResult result = race.overall_results[result_index];

        writer.append("""
                <tr>
                <td>""");
        if (!result.dnf()) writer.append(String.valueOf(result_index + 1));
        writer.append("""
                </td>
                <td>""");
        writer.append(String.valueOf(result.team.bib_number));
        writer.append("""
                </td>
                <td>""");
        writer.append(htmlEncode(result.team.name));
        writer.append("""
                </td>
                <td>""");
        writer.append(result.team.category.shortName());
        writer.append("""
                </td>""");

        printLegDetails(writer, result, result.team);

        writer.append("""
                </tr>""");
    }

   private void printDetailedResultsFooter(final OutputStreamWriter writer) throws IOException {

        writer.append("""
                </tbody>
            </table>
            """);
    }

    private void printLegResults(final OutputStreamWriter html_writer, int leg) throws IOException {

        printLegResultsHeader(html_writer, leg);
        printLegResultsBody(html_writer, getLegResults(leg));
        printLegResultsFooter(html_writer);
    }

    private void printLegDetails(OutputStreamWriter writer, LapRaceResult result, Team team) throws IOException {

        boolean any_previous_leg_dnf = false;

        for (int leg = 1; leg <= race.number_of_legs; leg++) {

            final LegResult leg_result = result.leg_results[leg - 1];

            writer.append("""
                <td>""");
            writer.append(htmlEncode(team.runners[leg - 1]));

            addMassStartAnnotation(writer, leg_result, leg);

            writer.append("""
                </td>
                <td>""");

            writer.append(leg_result.DNF ? DNF_STRING : format(leg_result.duration()));
            writer.append("""
                </td>
                <td>""");
            writer.append(leg_result.DNF || any_previous_leg_dnf ? DNF_STRING : format(sumDurationsUpToLeg(result.leg_results, leg)));
            writer.append("""
                </td>""");

            if (leg_result.DNF) any_previous_leg_dnf = true;
        }
    }

    private void printLegResultsHeader(final OutputStreamWriter writer, final int leg) throws IOException {

        writer.append("""
            <table class="fac-table">
                <thead>
                    <tr>
                        <th>Pos</th>
                        <th>Runner""");

        if (race.paired_legs[leg-1]) writer.append("s");

        writer.append("""
            </th>
                        <th>Time</th>
                    </tr>
                </thead>
                <tbody>
            """);
    }

    private void printLegResultsBody(final OutputStreamWriter writer, final LegResult[] leg_results) throws IOException {

        for (final LegResult leg_result : leg_results) {

            if (!leg_result.DNF) {
                writer.append("""
                                <tr>
                                <td>""");
                writer.append(leg_result.position_string);
                writer.append("""
                                </td>
                                <td>""");
                writer.append(htmlEncode(leg_result.team.runners[leg_result.leg_number-1]));
                writer.append("""
                                </td>
                                <td>""");
                writer.append(format(leg_result.duration()));
                writer.append("""
                                </td>
                            </tr>""");
            }
        }
    }

    private void printLegResultsFooter(final OutputStreamWriter writer) throws IOException {

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

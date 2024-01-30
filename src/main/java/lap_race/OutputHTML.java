package lap_race;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class OutputHTML extends Output {

    public OutputHTML(final Results results) {
        super(results);
    }

    @Override
    public void printPrizes() {

        throw new UnsupportedOperationException();
    }

    @Override
    public void printOverallResults() throws IOException {

        final Path overall_results_html_path = output_directory_path.resolve(overall_results_filename + ".html");

        try (final OutputStreamWriter html_writer = new OutputStreamWriter(Files.newOutputStream(overall_results_html_path))) {

            printOverallResultsHeader(html_writer);
            printOverallResults(html_writer);
            printOverallResultsFooter(html_writer);
        }
    }

    @Override
    public void printDetailedResults() throws IOException {

        final Path detailed_results_html_path = output_directory_path.resolve(detailed_results_filename + ".html");

        try (final OutputStreamWriter html_writer = new OutputStreamWriter(Files.newOutputStream(detailed_results_html_path))) {

            printDetailedResultsHeader(html_writer);
            printDetailedResults(html_writer);
            printDetailedResultsFooter(html_writer);
        }
    }

    public void printLegResults(final int leg) throws IOException {

        final Path leg_results_html_path = output_directory_path.resolve(race_name_for_filenames + "_leg_" + leg + "_" + year + ".html");

        try (final OutputStreamWriter html_writer = new OutputStreamWriter(Files.newOutputStream(leg_results_html_path))) {

            printLegResultsHeader(html_writer, leg);
            printLegResults(html_writer, getLegResults(leg));
            printLegResultsFooter(html_writer);
        }
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

    private void printOverallResults(final OutputStreamWriter writer) throws IOException {

        int position = 1;

        for (final OverallResult result : results.overall_results) {

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
            writer.append(String.valueOf(result.team.name));
            writer.append("""
                            </td>
                            <td>""");
            writer.append(String.valueOf(result.team.category));
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

        for (int leg_number = 1; leg_number <= results.number_of_legs; leg_number++) {

            writer.append("<th>Runners ").append(String.valueOf(leg_number)).append("</th>");
            writer.append("<th>Leg ").append(String.valueOf(leg_number)).append("</th>");

            if (leg_number < results.number_of_legs)
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

    private void printDetailedResults(final OutputStreamWriter writer) throws IOException {

        for (int result_index = 0; result_index < results.overall_results.length; result_index++)
            printDetailedResult(writer, result_index);
    }

    private void printDetailedResult(final OutputStreamWriter writer, final int result_index) throws IOException {

        final OverallResult result = results.overall_results[result_index];

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
        writer.append(String.valueOf(result.team.name));
        writer.append("""
                </td>
                <td>""");
        writer.append(String.valueOf(result.team.category));
        writer.append("""
                </td>""");

        printLegDetails(writer, result, result.team);

        writer.append("""
                </tr>""");
    }

    private void printLegDetails(OutputStreamWriter writer, OverallResult result, Team team) throws IOException {

        boolean any_previous_leg_dnf = false;

        for (int leg = 1; leg <= results.number_of_legs; leg++) {

            final LegResult leg_result = result.leg_results[leg - 1];

            writer.append("""
                <td>""");
            writer.append(team.runners[leg - 1]);

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

    private void printDetailedResultsFooter(final OutputStreamWriter writer) throws IOException {

        writer.append("""
                </tbody>
            </table>
            """);
    }

    private void printLegResultsHeader(final OutputStreamWriter writer, final int leg) throws IOException {

        writer.append("""
            <table class="fac-table">
                <thead>
                    <tr>
                        <th>Pos</th>
                        <th>Runner""");

        if (results.paired_legs[leg-1]) writer.append("s");

        writer.append("""
            </th>
                        <th>Time</th>
                    </tr>
                </thead>
                <tbody>
            """);
    }

    private void printLegResults(final OutputStreamWriter writer, final LegResult[] leg_results) throws IOException {

        for (final LegResult leg_result : leg_results) {

            if (!leg_result.DNF) {
                writer.append("""
                                <tr>
                                <td>""");
                writer.append(leg_result.position_string);
                writer.append("""
                                </td>
                                <td>""");
                writer.append(leg_result.team.runners[leg_result.leg_number-1]);
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
}

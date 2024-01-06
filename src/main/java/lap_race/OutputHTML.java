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

        final Path overall_results_html_path = results.output_directory_path.resolve(results.overall_results_filename + ".html");

        try (final OutputStreamWriter html_writer = new OutputStreamWriter(Files.newOutputStream(overall_results_html_path))) {

            printOverallResultsHTMLHeader(html_writer);
            printOverallResultsHTML(html_writer);
            printOverallResultsHTMLFooter(html_writer);
        }
    }

    @Override
    public void printDetailedResults() throws IOException {

        final Path detailed_results_html_path = results.output_directory_path.resolve(results.detailed_results_filename + ".html");

        try (final OutputStreamWriter html_writer = new OutputStreamWriter(Files.newOutputStream(detailed_results_html_path))) {

            printDetailedResultsHTMLHeader(html_writer);
            printDetailedResultsHTML(html_writer);
            printDetailedResultsHTMLFooter(html_writer);
        }
    }

    void printLegResults(final int leg) throws IOException {

        final Path leg_results_html_path = results.output_directory_path.resolve(results.race_name_for_filenames + "_leg_" + leg + "_" + results.year + ".html");

        try (final OutputStreamWriter html_writer = new OutputStreamWriter(Files.newOutputStream(leg_results_html_path))) {

            printLegResultsHTMLHeader(leg, html_writer);
            printLegResultsHTML(results.getLegResults(leg), html_writer);
            printLegResultsHTMLFooter(html_writer);
        }
    }

    private void printOverallResultsHTMLHeader(final OutputStreamWriter writer) throws IOException {

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

    private void printOverallResultsHTML(final OutputStreamWriter writer) throws IOException {

        int position = 1;

        for (final OverallResult result : results.overall_results) {

            writer.append("""
                            <tr>
                            <td>""");
            writer.append(String.valueOf(position++));
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
            writer.append(OverallResult.format(result.duration()));
            writer.append("""
                            </td>
                        </tr>""");
        }
    }

    private void printOverallResultsHTMLFooter(final OutputStreamWriter writer) throws IOException {

        writer.append("""
                </tbody>
            </table>
            """);
    }

    private void printDetailedResultsHTMLHeader(final OutputStreamWriter writer) throws IOException {

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

    private void printDetailedResultsHTML(final OutputStreamWriter writer) throws IOException {

        int position = 1;

        for (final OverallResult result : results.overall_results) {

            final Team team = result.team;
            boolean any_previous_leg_dnf = false;

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
                    </td>""");

            for (int leg = 1; leg <= results.number_of_legs; leg++) {

                final LegResult leg_result = result.leg_results[leg - 1];

                writer.append("""
                    <td>""");
                writer.append(team.runners[leg - 1]);
                if (leg_result.in_mass_start)
                    writer.append(" (M").append(String.valueOf(leg)).append(")");
                writer.append("""
                    </td>
                    <td>""");

                writer.append(leg_result.DNF ? Results.DNF_STRING : OverallResult.format(leg_result.duration()));
                writer.append("""
                    </td>
                    <td>""");
                writer.append(leg_result.DNF || any_previous_leg_dnf ? Results.DNF_STRING : OverallResult.format(results.sumDurationsUpToLeg(result.leg_results, leg)));
                writer.append("""
                    </td>""");
                if (leg_result.DNF) any_previous_leg_dnf = true;
            }

            writer.append("""
                    </tr>""");
        }
    }

    private void printDetailedResultsHTMLFooter(final OutputStreamWriter writer) throws IOException {

        writer.append("""
                </tbody>
            </table>
            """);
    }

    private void printLegResultsHTMLHeader(final int leg, final OutputStreamWriter writer) throws IOException {

        writer.append("""
            <table class="fac-table">
                <thead>
                    <tr>
                        <th>Pos</th>
                        <th>Runner
            """);

        if (results.paired_legs[leg-1]) writer.append("s");

        writer.append("""
            </th>
                        <th>Time</th>
                    </tr>
                </thead>
                <tbody>
            """);
    }

    private void printLegResultsHTML(final LegResult[] leg_results, final OutputStreamWriter writer) throws IOException {

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
                writer.append(OverallResult.format(leg_result.duration()));
                writer.append("""
                                </td>
                            </tr>""");
            }
        }
    }    private void printLegResultsHTMLFooter(final OutputStreamWriter writer) throws IOException {

        writer.append("""
                </tbody>
            </table>
            """);
    }

}

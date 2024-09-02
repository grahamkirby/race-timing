package relay_race;

import common.categories.Category;
import common.output.RaceOutputHTML;
import common.RaceResult;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class RelayRaceOutputHTML extends RaceOutputHTML {

    private String detailed_results_filename;

    public RelayRaceOutputHTML(final RelayRace race) {
        super(race);
        constructFilePaths();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public void printLegResults() throws IOException {

        for (int leg = 1; leg <= ((RelayRace)race).number_of_legs; leg++)
            printLegResults(leg);
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

    @Override
    public void printPrizes() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(prizes_filename + ".html"));

        try (final OutputStreamWriter html_writer = new OutputStreamWriter(stream)) {
            printPrizes(html_writer);
        }
    }

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

            html_writer.append("""
                    <h4>Full Results</h4>
                    """);

            printDetailedResults(html_writer);

            html_writer.append("""
                    <p>M3: mass start leg 3<br />M4: mass start leg 4</p>
                    """);

            for (int leg_number = 1; leg_number <= ((RelayRace)race).number_of_legs; leg_number++) {

                html_writer.append("<p></p>\n<h4>Leg ").append(String.valueOf(leg_number)).append(" Results</h4>\n");
                printLegResults(html_writer, leg_number);
            }
        }
    }

    @Override
    protected void constructFilePaths() {

        super.constructFilePaths();
        detailed_results_filename = race_name_for_filenames + "_detailed_" + year;
    }

    @Override
    protected void printPrizes(final OutputStreamWriter writer) throws IOException {

        writer.append("<h4>Prizes</h4>\n");

        for (final Category category : race.categories.getPrizeCategoriesInReportOrder())
            printPrizes(writer, category);
    }

    @Override
    protected void printPrizes(final OutputStreamWriter writer, final Category category) throws IOException {

        writer.append("<p><strong>").append(category.getLongName()).append("</strong></p>\n");
        writer.append("<ol>\n");

        final List<RaceResult> category_prize_winners = race.prize_winners.get(category);

        if (category_prize_winners == null)
            writer.append("No results\n");
        else {
            for (final RaceResult r : category_prize_winners) {

                final RelayRaceResult result = ((RelayRaceResult) r);

                writer.append("<li>").
                        append(result.entry.team.name).append(" (").
                        append(result.entry.team.category.getLongName()).append(") ").
                        append(format(result.duration())).append("</li>\n");
            }
        }

        writer.append("</ol>\n\n");
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
                                       <th>No</th>
                                       <th>Team</th>
                                       <th>Category</th>
                                       <th>Total</th>
                                   </tr>
                               </thead>
                               <tbody>
            """);
    }

    private void printLegResults(final int leg) throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(race_name_for_filenames + "_leg_" + leg + "_" + year + ".html"));

        try (final OutputStreamWriter html_writer = new OutputStreamWriter(stream)) {
            printLegResults(html_writer, leg);
        }
    }

    private void printOverallResultsBody(final OutputStreamWriter writer) throws IOException {

        int position = 1;

        for (final RaceResult res : race.getOverallResults()) {

            RelayRaceResult result = ((RelayRaceResult) res);

            writer.append("""
                        <tr>
                            <td>""");
            if (!result.dnf()) writer.append(String.valueOf(position++));
            writer.append("""
                            </td>
                            <td>""");
            writer.append(String.valueOf(result.entry.bib_number));
            writer.append("""
                            </td>
                            <td>""");
            writer.append(htmlEncode(result.entry.team.name));
            writer.append("""
                            </td>
                            <td>""");
            writer.append(result.entry.team.category.getLongName());
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

    private void printDetailedResults(final OutputStreamWriter writer) throws IOException {

        printDetailedResultsHeader(writer);
        printDetailedResultsBody(writer);
        printDetailedResultsFooter(writer);
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

        for (int leg_number = 1; leg_number <= ((RelayRace)race).number_of_legs; leg_number++) {

            writer.append("<th>Runner");
            if (((RelayRace)race).paired_legs.get(leg_number-1)) writer.append("s");
            writer.append(" ").append(String.valueOf(leg_number)).append("</th>");

            writer.append("<th>Leg ").append(String.valueOf(leg_number)).append("</th>");

            if (leg_number < ((RelayRace)race).number_of_legs)
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

        for (int result_index = 0; result_index < race.getOverallResults().size(); result_index++)
            printDetailedResult(writer, result_index);
    }

    private void printDetailedResult(final OutputStreamWriter writer, final int result_index) throws IOException {

        final RelayRaceResult result = (RelayRaceResult) race.getOverallResults().get(result_index);

        writer.append("""
                <tr>
                <td>""");
        if (!result.dnf()) writer.append(String.valueOf(result_index + 1));
        writer.append("""
                </td>
                <td>""");
        writer.append(String.valueOf(result.entry.bib_number));
        writer.append("""
                </td>
                <td>""");
        writer.append(htmlEncode(result.entry.team.name));
        writer.append("""
                </td>
                <td>""");
        writer.append(result.entry.team.category.getLongName());
        writer.append("""
                </td>""");

        printLegDetails(writer, result, result.entry.team);

        writer.append("""
                </tr>""");
    }

    private void printDetailedResultsFooter(final OutputStreamWriter writer) throws IOException {

        writer.append("""
                </tbody>
            </table>
            """);
    }

    private void printLegResults(final OutputStreamWriter writer, final int leg) throws IOException {

        printLegResultsHeader(writer, leg);
        printLegResultsBody(writer, getLegResults(leg));
        printLegResultsFooter(writer);
    }

    private void printLegDetails(final OutputStreamWriter writer, final RelayRaceResult result, final Team team) throws IOException {

        boolean any_previous_leg_dnf = false;

        for (int leg = 1; leg <= ((RelayRace)race).number_of_legs; leg++) {

            final LegResult leg_result = result.leg_results.get(leg - 1);

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

    private Duration sumDurationsUpToLeg(final List<LegResult> leg_results, final int leg) {

        Duration total = Duration.ZERO;
        for (int i = 0; i < leg; i++)
            total = total.plus(leg_results.get(i).duration());
        return total;
    }

    private List<LegResult> getLegResults(final int leg_number) {

        final List<LegResult> leg_results = new ArrayList<>();

        for (final RaceResult overall_result : race.getOverallResults())
            leg_results.add(((RelayRaceResult)overall_result).leg_results.get(leg_number - 1));

        // Sort in order of increasing overall leg time, as defined in LegResult.compareTo().
        // Ordering for DNF results doesn't matter since they're omitted in output.
        // Where two teams have the same overall time, the order in which their last leg runners were recorded is preserved.
        // OutputCSV.printLegResults deals with dead heats.
        leg_results.sort(LegResult::compareTo);

        return leg_results;
    }

    private void addMassStartAnnotation(final OutputStreamWriter writer, final LegResult leg_result, final int leg) throws IOException {

        // Adds e.g. "(M3)" after names of runners that started in leg 3 mass start.
        if (leg_result.in_mass_start) {

            // Find the next mass start.
            int mass_start_leg = leg;
            while (!((RelayRace)race).mass_start_legs.get(mass_start_leg-1))
                mass_start_leg++;

            writer.append(" (M").append(String.valueOf(mass_start_leg)).append(")");
        }
    }
    
    private void printLegResultsHeader(final OutputStreamWriter writer, final int leg) throws IOException {

        writer.append("""
            <table class="fac-table">
                <thead>
                    <tr>
                        <th>Pos</th>
                        <th>Runner""");

        if (((RelayRace)race).paired_legs.get(leg-1)) writer.append("s");

        writer.append("""
            </th>
                        <th>Time</th>
                    </tr>
                </thead>
                <tbody>
            """);
    }

    private void printLegResultsBody(final OutputStreamWriter writer, final List<LegResult> leg_results) throws IOException {

        for (final LegResult leg_result : leg_results) {

            if (!leg_result.DNF) {
                writer.append("""
                                <tr>
                                <td>""");
                writer.append(leg_result.position_string);
                writer.append("""
                                </td>
                                <td>""");
                writer.append(htmlEncode(leg_result.entry.team.runners[leg_result.leg_number-1]));
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

package individual_race;

import common.Category;
import common.RaceResult;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;

public class IndividualRaceOutputHTML extends IndividualRaceOutput {

    public IndividualRaceOutputHTML(final IndividualRace race) {
        super(race);
    }

    @Override
    public void printPrizes() throws IOException {

        printPrizesHTML();
    }

    @Override
    public void printOverallResults() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(overall_results_filename + ".html"));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            printOverallResults(writer);
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
        }
    }

    private void printPrizes(final OutputStreamWriter html_writer) throws IOException {

        html_writer.append("<h4>Prizes</h4>\n");

        for (final Category category : race.categories.getCategoriesInReportOrder())
            printPrizes(category, html_writer);
    }

    public void printPrizes(final Category category, final OutputStreamWriter writer) throws IOException {

        final List<RaceResult> category_prize_winners = ((IndividualRace)race).prize_winners.get(category);

        if (category_prize_winners != null) {
            writer.append("<p><strong>").append(category.getLongName()).append("</strong></p>\n");
            writer.append("<ol>\n");

            if (category_prize_winners.isEmpty())
                writer.append("No results\n");
            else
                for (final RaceResult entry : category_prize_winners) {

                    final IndividualRaceResult result = ((IndividualRaceResult)entry);

                    writer.append("<li>").
                            append(htmlEncode(result.entry.runner.name)).append(" (").
                            append((result.entry.runner.club)).append(") ").
                            append(format(result.duration())).append("</li>\n");
                }

            writer.append("</ol>\n\n");
        }
    }

    @Override
    protected void printOverallResults(final OutputStreamWriter html_writer) throws IOException {

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
                                       <th>No</th>
                                       <th>Runner</th>
                                       <th>Club</th>
                                       <th>Cat</th>
                                       <th>Time</th>
                                   </tr>
                               </thead>
                               <tbody>
            """);
    }

    private void printOverallResultsBody(final OutputStreamWriter writer) throws IOException {

        int position = 1;

        for (final IndividualRaceResult result : ((IndividualRace)race).getOverallResults()) {

            writer.append("""
                        <tr>
                            <td>""");
            if (!result.DNF) writer.append(String.valueOf(position++));
            writer.append("""
                            </td>
                            <td>""");
            writer.append(String.valueOf(result.entry.bib_number));
            writer.append("""
                            </td>
                            <td>""");
            writer.append(htmlEncode(result.entry.runner.name));
            writer.append("""
                            </td>
                            <td>""");
//            writer.append(normaliseClubName(result.entry.runner.club));
            writer.append((result.entry.runner.club));
            writer.append("""
                            </td>
                            <td>""");
            writer.append(result.entry.runner.category.getShortName());
            writer.append("""
                            </td>
                            <td>""");
            writer.append(result.DNF ? DNF_STRING : format(result.duration()));
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
}

package minitour;

import com.lowagie.text.Document;
import common.Category;
import individual_race.IndividualRace;
import individual_race.IndividualRaceResult;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

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
    protected void printPrizes(Category category, Document document) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printOverallResults() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(overall_results_filename + ".html"));

        try (final OutputStreamWriter html_writer = new OutputStreamWriter(stream)) {
            printOverallResults(html_writer);
        }
    }

    public void printCombined() throws IOException {

        for (int i = 1; i <= ((MinitourRace)race).races.length; i++)
            printRace(i);

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve("combined.html"));

        try (final OutputStreamWriter html_writer = new OutputStreamWriter(stream)) {

            html_writer.append("""
                    <h3><strong>Results</strong></h3>
                    """);

            printPrizes(html_writer);

            printOverallResults(html_writer);
        }
    }

    private void printRace(final int race_number) throws IOException {

        final IndividualRace individual_race = ((MinitourRace)race).races[race_number - 1];

        if (individual_race != null) {

            final OutputStream race_stream = Files.newOutputStream(output_directory_path.resolve("race" + race_number + ".html"));

            try (final OutputStreamWriter html_writer = new OutputStreamWriter(race_stream)) {

                printRaceCategories(html_writer, individual_race, "U9", "FU9","MU9");
                printRaceCategories(html_writer, individual_race, "U11", "FU11", "MU11");
                printRaceCategories(html_writer, individual_race, "U13", "FU13", "MU13");
                printRaceCategories(html_writer, individual_race, "U15", "FU15", "MU15");
                printRaceCategories(html_writer, individual_race, "U18", "FU18", "MU18");
            }
        }
    }

    private void printRaceCategories(final OutputStreamWriter html_writer, final IndividualRace individualRace, final String combined_categories_title, final String... category_names) throws IOException {

        final List<Category> category_list = getCategoryList(category_names);

        final IndividualRaceResult[] category_results = Stream.of(individualRace.getOverallResults()).
                filter(result -> category_list.contains(result.entry.runner.category)).
                toArray(IndividualRaceResult[]::new);

        printRaceCategories(html_writer, category_results, combined_categories_title);
    }

    private void printRaceCategories(final OutputStreamWriter html_writer, final IndividualRaceResult[] category_results, final String combined_categories_title) throws IOException {

        html_writer.append("<h4>").
                append(combined_categories_title).
                append("</h4>\n").
                append("""
                    <table class="fac-table">
                                   <thead>
                                       <tr>
                                           <th>Pos</th>
                                           <th>No</th>
                                           <th>Runner</th>
                                           <th>Category</th>
                                           <th>Total</th>
                                       </tr>
                                   </thead>
                                   <tbody>
                """);

        printRaceCategories(html_writer, category_results);

        html_writer.append("""
                    </tbody>
                </table>
                """);
    }

    private static void printRaceCategories(final OutputStreamWriter html_writer, final IndividualRaceResult[] category_results) throws IOException {

        int position = 1;

        for (final IndividualRaceResult result : category_results) {

            html_writer.append("""
                    <tr>
                        <td>""");

            if (!result.dnf()) html_writer.append(String.valueOf(position++));

            html_writer.append("""
                    </td>
                    <td>""").
            append(String.valueOf(result.entry.bib_number)).
            append("""
                    </td>
                    <td>""").
            append(htmlEncode(result.entry.runner.name)).
            append("""
                    </td>
                    <td>""").
            append(result.entry.runner.category.getShortName()).
            append("""
                    </td>
                    <td>""").
            append(result.dnf() ? DNF_STRING : format(result.duration())).
            append("""
                        </td>
                    </tr>""");
        }
    }

    private void printPrizes(final OutputStreamWriter html_writer) throws IOException {

        html_writer.append("<h4>Prizes</h4>\n");

        for (final Category category : race.categories.getCategoriesInReportOrder())
            printPrizes(category, html_writer);
    }

    private void printPrizes(final Category category, final OutputStreamWriter writer) throws IOException {

        writer.append("<p><strong>").append(category.getShortName()).append("</strong></p>\n");
        writer.append("<ul>\n");

        final MinitourRaceResult[] category_prize_winner_results = getMinitourRacePrizeResults(category);

        printResults(category_prize_winner_results, new PrizeResultPrinterHTML(((MinitourRace)race), writer));

        writer.append("</ul>\n\n");
    }

    private void printOverallResults(final OutputStreamWriter writer) throws IOException {

        writer.append("<h4>Overall Results</h4>\n");

        printOverallResults(writer, "U9", "FU9", "MU9");
        printOverallResults(writer, "U11", "FU11", "MU11");
        printOverallResults(writer, "U13", "FU13", "MU13");
        printOverallResults(writer, "U15", "FU15", "MU15");
        printOverallResults(writer, "U18", "FU18", "MU18");
    }

    private void printOverallResults(final OutputStreamWriter writer, final String combined_categories_title, final String... category_names) throws IOException {

        final List<Category> category_list = getCategoryList(category_names);

        writer.append("<h4>").append(combined_categories_title).append("</h4>\n");

        printOverallResultsHeader(writer);
        printOverallResultsBody(writer, category_list);
        printOverallResultsFooter(writer);
    }

    private List<Category> getCategoryList(final String... category_names) {

        return Arrays.stream(category_names).map(s -> race.categories.getCategory(s)).toList();
    }

    private void printOverallResultsHeader(final OutputStreamWriter writer) throws IOException {

        writer.append("""
                <table class="fac-table">
                               <thead>
                                   <tr>
                                       <th>Pos</th>
                                       <th>Runner</th>
                                       <th>Cat</th>
                                       <th>Club</th>
            """);

        for (int i = 0; i < ((MinitourRace)race).races.length; i++)
            if (((MinitourRace)race).races[i] != null)
                writer.append("<th>Race ").append(String.valueOf(i + 1)).append("</th>\n");

        writer.append("""
                                       <th>Total</th>
                                   </tr>
                               </thead>
                               <tbody>
            """);
    }

    private void printOverallResultsBody(final OutputStreamWriter writer, final List<Category> result_categories) throws IOException {

        printResults(((MinitourRace)race).getCompletedResultsByCategory(result_categories), new OverallResultPrinterHTML(writer));
    }

    private void printOverallResultsFooter(final OutputStreamWriter writer) throws IOException {

        writer.append("""
                </tbody>
            </table>
            """);
    }

    record OverallResultPrinterHTML(OutputStreamWriter writer) implements ResultPrinter {

        @Override
        public void printResult(final MinitourRaceResult result) throws IOException {

            writer.append("""
                    <tr>
                        <td>""").
                    append(result.completedAllRacesSoFar() ? result.position_string : "-").
                    append("""
                        </td>
                        <td>""").
                    append(htmlEncode(result.runner.name)).
                    append("""
                        </td>
                        <td>""").
                    append(result.runner.category.getShortName()).
                    append("""
                        </td>
                        <td>""").
                    append(result.runner.club).
                    append("""
                        </td>""");


            for (int i = 0; i < result.times.length; i++)
                if (result.times[i] != null)
                    writer.append("<td>").
                            append(format(result.times[i])).
                            append("</td>\n");
                else
                    if (result.raceHasTakenPlace(i + 1))
                        writer.append("<td>").
                                append("-").
                                append("</td>\n");

            writer.append("""
                        <td>""").
                    append(result.completedAllRacesSoFar() ? format(result.duration()) : "-").
                    append("""
                        </td>
                    </tr>""");
        }

        @Override
        public void printNoResults() throws IOException {

            writer.append("No results\n");
        }
    }

    record PrizeResultPrinterHTML(MinitourRace race, OutputStreamWriter writer) implements ResultPrinter {

        @Override
        public void printResult(MinitourRaceResult result) throws IOException {

            final Duration time = race.getOverallResults()[race.findIndexOfRunner(result.runner)].duration();

            writer.append("<li>").
                    append(result.position_string).
                    append(" ").
                    append(htmlEncode(result.runner.name)).
                    append(" (").
                    append(result.runner.category.getShortName()).
                    append(") ").
                    append(format(time)).
                    append("</li>\n");
        }

        @Override
        public void printNoResults() throws IOException {

            writer.append("No results\n");
        }
    }
}

package minitour;

import common.Category;
import individual_race.IndividualRace;
import individual_race.IndividualRaceResult;
import individual_race.Runner;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static common.Race.format;
import static series_race.SeriesRaceOutputHTML.htmlEncode;

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

        for (int i = 1; i <= race.races.length; i++)
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

        final IndividualRace individual_race = race.races[race_number - 1];

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

        html_writer.append("<h4>").append(combined_categories_title).append("</h4>\n");

        html_writer.append("""
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

        final List<Category> category_list = getCategoryList(category_names);

        final IndividualRaceResult[] category_results = Stream.of(individualRace.getOverallResults()).filter(
                individualRaceResult -> category_list.contains(individualRaceResult.entry.runner.category)).
                toList().toArray(new IndividualRaceResult[0]);

        int position = 1;

        for (final IndividualRaceResult result : category_results) {

            html_writer.append("""
                    <tr>
                        <td>""");
            if (!result.dnf()) html_writer.append(String.valueOf(position++));
            html_writer.append("""
                    </td>
                    <td>""");
            html_writer.append(String.valueOf(result.entry.bib_number));
            html_writer.append("""
                    </td>
                    <td>""");
            html_writer.append(htmlEncode(result.entry.runner.name));
            html_writer.append("""
                    </td>
                    <td>""");
            html_writer.append(result.entry.runner.category.getShortName());
            html_writer.append("""
                    </td>
                    <td>""");
            html_writer.append(result.dnf() ? DNF_STRING : format(result.duration()));
            html_writer.append("""
                        </td>
                    </tr>""");
        }

        html_writer.append("""
                    </tbody>
                </table>
                """);
    }

    private void printPrizes(final OutputStreamWriter html_writer) throws IOException {

        html_writer.append("<h4>Prizes</h4>\n");

        for (final Category category : race.categories.getCategoriesInReportOrder())
            printPrizes(category, html_writer);
    }

    private void printPrizes(final Category category, final OutputStreamWriter writer) throws IOException {

        final List<Runner> category_prize_winners = race.prize_winners.get(category);

        final MinitourRaceResult[] category_prize_winner_results = new MinitourRaceResult[category_prize_winners.size()];
        for (int i = 0; i < category_prize_winners.size(); i++) {
            for (MinitourRaceResult result : race.overall_results) {
                if (result.runner.equals(category_prize_winners.get(i))) {
                    category_prize_winner_results[i] = result;
                    break;
                }
            }
        }

        setPositionStrings(category_prize_winner_results);

        writer.append("<p><strong>").append(category.getShortName()).append("</strong></p>\n");
        writer.append("<ul>\n");

        if (category_prize_winners.isEmpty())
            writer.append("No results\n");
        else
            for (final MinitourRaceResult result : category_prize_winner_results)
                print_prize_winner(writer, result);

        writer.append("</ul>\n\n");
    }

    private void print_prize_winner(final OutputStreamWriter writer, final MinitourRaceResult result) throws IOException {

        final Duration time = race.getOverallResults()[race.findIndexOfRunner(result.runner)].duration();

        writer.append("<li>").
                append(result.position_string).append(" ").
                append(htmlEncode(result.runner.name)).append(" (").
                append(result.runner.category.getShortName()).append(") ").
                append(format(time)).append("</li>\n");
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

        for (int i = 0; i < race.races.length; i++) {
            if (race.races[i] != null) {
                writer.append("<th>Race ").append(String.valueOf(i + 1)).append("</th>\n");
            }
        }

        writer.append("""
                                       <th>Total</th>
                                   </tr>
                               </thead>
                               <tbody>
            """);
    }

    private void printOverallResultsBody(final OutputStreamWriter writer, final List<Category> result_categories) throws IOException {

        final MinitourRaceResult[] category_results = race.getCompletedResultsByCategory(result_categories);

        setPositionStrings(category_results);

        for (final MinitourRaceResult result : category_results) {

            writer.append("""
                        <tr>
                            <td>""");
            writer.append(result.position_string);
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
                            </td>""");
            for (int i = 0; i < result.times.length; i++) {
                if (result.times[i] != null) {
                    writer.append("<td>").append(format(result.times[i])).append("</td>\n");
                }
            }
            writer.append("""
                            <td>""");
            writer.append(format(result.duration()));
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

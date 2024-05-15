package minitour;

import common.Category;
import individual_race.IndividualRace;
import individual_race.IndividualRaceOutput;
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
import static minitour.MinitourRaceOutputCSV.setPositionStrings;
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

    private void printRace(int race_number) throws IOException {

        IndividualRace individualRace = race.races[race_number - 1];

        if (individualRace != null) {
            final OutputStream race_stream = Files.newOutputStream(output_directory_path.resolve("race" + race_number + ".html"));

            try (final OutputStreamWriter html_writer = new OutputStreamWriter(race_stream)) {

                IndividualRaceResult[] overallResults = individualRace.getOverallResults();

                printRaceCategories(html_writer, filterResultsByCategory(overallResults, Arrays.asList(race.categories.getCategory("FU9"), race.categories.getCategory("MU9"))), "U9");
                printRaceCategories(html_writer, filterResultsByCategory(overallResults, Arrays.asList(race.categories.getCategory("FU11"), race.categories.getCategory("MU11"))), "U11");
                printRaceCategories(html_writer, filterResultsByCategory(overallResults, Arrays.asList(race.categories.getCategory("FU13"), race.categories.getCategory("MU13"))), "U13");
                printRaceCategories(html_writer, filterResultsByCategory(overallResults, Arrays.asList(race.categories.getCategory("FU15"), race.categories.getCategory("MU15"))), "U15");
                printRaceCategories(html_writer, filterResultsByCategory(overallResults, Arrays.asList(race.categories.getCategory("FU18"), race.categories.getCategory("MU18"))), "U18");
            }
        }
    }

    private IndividualRaceResult[] filterResultsByCategory(IndividualRaceResult[] overallResults, List<Category> list) {

        return Stream.of(overallResults).filter(individualRaceResult -> list.contains(individualRaceResult.entry.runner.category)).toList().toArray(new IndividualRaceResult[0]);
    }

    private static void printRaceCategories(OutputStreamWriter html_writer, IndividualRaceResult[] overallResults, String combined_categories_title) throws IOException {

        html_writer.append("<h4>" + combined_categories_title + "</h4>\n");

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

        int position = 1;

        for (final IndividualRaceResult result : overallResults) {

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
                        append(htmlEncode(entry.name)).append(" (").
                        append(entry.category.getShortName()).append(") ").
                        append(format(time)).append("</li>\n");
            }

            writer.append("</ol>\n\n");
        }
    }

    private void printOverallResults(OutputStreamWriter html_writer) throws IOException {

        html_writer.append("<h4>Overall Results</h4>\n");

        printOverallResults(html_writer, Arrays.asList(race.categories.getCategory("FU9"), race.categories.getCategory("MU9")), "U9");
        printOverallResults(html_writer, Arrays.asList(race.categories.getCategory("FU11"), race.categories.getCategory("MU11")), "U11");
        printOverallResults(html_writer, Arrays.asList(race.categories.getCategory("FU13"), race.categories.getCategory("MU13")), "U13");
        printOverallResults(html_writer, Arrays.asList(race.categories.getCategory("FU15"), race.categories.getCategory("MU15")), "U15");
        printOverallResults(html_writer, Arrays.asList(race.categories.getCategory("FU18"), race.categories.getCategory("MU18")), "U18");
    }

    private void printOverallResults(OutputStreamWriter html_writer, List<Category> result_categories, String combined_categories_title) throws IOException {

        html_writer.append("<h4>" + combined_categories_title + "</h4>\n");

        printOverallResultsHeader(html_writer);
        printOverallResultsBody(html_writer, result_categories);
        printOverallResultsFooter(html_writer);
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

    private void printOverallResultsBody(final OutputStreamWriter writer, List<Category> result_categories) throws IOException {

        final MinitourRaceResult[] series_results = race.getOverallResults(result_categories);

        setPositionStrings(series_results);

        for (final MinitourRaceResult result : series_results) {

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

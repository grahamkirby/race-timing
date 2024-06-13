package fife_ac_races.minitour;

import common.Category;
import common.Race;
import common.RaceOutputHTML;
import common.RaceResult;
import individual_race.IndividualRace;
import individual_race.IndividualRaceResult;
import series_race.SeriesRace;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class MinitourRaceOutputHTML extends RaceOutputHTML {

    public MinitourRaceOutputHTML(final MinitourRace race) {
        super(race);
    }

    @Override
    public void printPrizes() throws IOException {

        printPrizesHTML();
    }

    @Override
    public void printOverallResults() throws IOException {

        printOverallResultsHTML();
    }

    @Override
    public void printCombined() throws IOException {

        for (int i = 1; i <= ((SeriesRace)race).races.size(); i++)
            printRace(i);

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve("combined.html"));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append("""
                    <h3><strong>Results</strong></h3>
                    """);

            printPrizesHTML(writer);
            printOverallResults(writer);
        }
    }

    private void printRace(final int race_number) throws IOException {

        final IndividualRace individual_race = ((SeriesRace)race).races.get(race_number - 1);

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

    private void printRaceCategories(final OutputStreamWriter writer, final Race race, final String combined_categories_title, final String... category_names) throws IOException {

        final List<Category> category_list = getCategoryList(category_names);

        final List<RaceResult> category_results = race.
                getOverallResults().
                stream().
                filter(result -> category_list.contains(((IndividualRaceResult)result).entry.runner.category)).
                toList();

        printRaceCategories(writer, category_results, combined_categories_title);
    }

    private void printRaceCategories(final OutputStreamWriter writer, final List<RaceResult> category_results, final String combined_categories_title) throws IOException {

        writer.append("<h4>").
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

        printRaceCategories(writer, category_results);

        writer.append("""
                    </tbody>
                </table>
                """);
    }

    private static void printRaceCategories(final OutputStreamWriter writer, final List<RaceResult> category_results) throws IOException {

        int position = 1;

        for (final RaceResult res : category_results) {

            final IndividualRaceResult result = (IndividualRaceResult) res;

            writer.append("""
                    <tr>
                        <td>""");

            if (!result.DNF) writer.append(String.valueOf(position++));

            writer.append("""
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
            append(result.DNF ? DNF_STRING : format(result.duration())).
            append("""
                        </td>
                    </tr>""");
        }
    }

    public void printPrizes(final Category category, final OutputStreamWriter writer) throws IOException {

        final List<RaceResult> results = race.prize_winners.get(category);

        writer.append("<p><strong>").append(category.getShortName()).append("</strong></p>\n");
        writer.append("<ul>\n");

        setPositionStrings(results, true);
        printResults(results, new PrizeResultPrinterHTML(((MinitourRace)race), writer));

        writer.append("</ul>\n\n");
    }

    @Override
    protected void printOverallResults(final OutputStreamWriter writer) throws IOException {

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

    @Override
    protected void printOverallResultsHeader(final OutputStreamWriter writer) throws IOException {

        writer.append("""
                <table class="fac-table">
                               <thead>
                                   <tr>
                                       <th>Pos</th>
                                       <th>Runner</th>
                                       <th>Cat</th>
                                       <th>Club</th>
            """);

        final SeriesRace series_race = (SeriesRace) race;

        for (int i = 0; i < series_race.races.size(); i++)
            if (series_race.races.get(i) != null)
                writer.append("<th>Race ").append(String.valueOf(i + 1)).append("</th>\n");

        writer.append("""
                                       <th>Total</th>
                                   </tr>
                               </thead>
                               <tbody>
            """);
    }

    private void printOverallResultsBody(final OutputStreamWriter writer, final List<Category> result_categories) throws IOException {

        final List<? extends RaceResult> results = ((MinitourRace) race).getResultsByCategory(result_categories);

        setPositionStrings(results, true);
        printResults(results, new OverallResultPrinterHTML(writer));
    }

    private void printOverallResultsFooter(final OutputStreamWriter writer) throws IOException {

        writer.append("""
                </tbody>
            </table>
            """);
    }

    record OverallResultPrinterHTML(OutputStreamWriter writer) implements ResultPrinter {

        @Override
        public void printResult(final RaceResult r) throws IOException {

            MinitourRaceResult result = (MinitourRaceResult)r;

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

            for (int i = 0; i < result.times.size(); i++)
                if (result.times.get(i) != null)
                    writer.append("<td>").
                            append(format(result.times.get(i))).
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
        public void printResult(RaceResult r) throws IOException {

            MinitourRaceResult result = (MinitourRaceResult)r;

            writer.append("<li>").
                    append(result.position_string).
                    append(" ").
                    append(htmlEncode(result.runner.name)).
                    append(" (").
                    append((result.runner.club)).
                    append(") ").
                    append(format(result.duration())).
                    append("</li>\n");
        }

        @Override
        public void printNoResults() throws IOException {

            writer.append("No results\n");
        }
    }
}

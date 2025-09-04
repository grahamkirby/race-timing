/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (race-timing@kirby-family.net)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.series_race;


import org.grahamkirby.race_timing.categories.PrizeCategory;
import org.grahamkirby.race_timing.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing.common.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.grahamkirby.race_timing.common.Config.renderDuration;
import static org.grahamkirby.race_timing.common.Normalisation.format;

class TourRaceOutputHTML {

    private final Race race;

    TourRaceOutputHTML(final Race race) {
        this.race = race;
    }

    void printResults() throws IOException {

        final String race_name = (String) race.getConfig().get(Config.KEY_RACE_NAME_FOR_FILENAMES);
        final String year = (String) race.getConfig().get(Config.KEY_YEAR);

        final OutputStream stream = getOutputStream(race_name, "overall", year, Config.STANDARD_FILE_OPEN_OPTIONS);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            final ResultPrinter printer = new OverallResultPrinter(race, writer);
            printResults(writer, printer, this::getResultsSubHeader, race);
        }
    }

    void printCombined() throws IOException {

        final String race_name = (String) race.getConfig().get(Config.KEY_RACE_NAME_FOR_FILENAMES);
        final String year = (String) race.getConfig().get(Config.KEY_YEAR);

        try (final OutputStreamWriter writer = new OutputStreamWriter(getOutputStream(race_name, "combined", year, Config.STANDARD_FILE_OPEN_OPTIONS))) {

            writer.append("<h3>Results</h3>").append(Config.LINE_SEPARATOR);

            writer.append(getPrizesHeader());
            printPrizes(writer);

            writer.append("<h4>Overall</h4>").append(Config.LINE_SEPARATOR);
            final ResultPrinter printer = new OverallResultPrinter(race, writer);

            printResults(writer, printer, this::getResultsSubHeader, race);

            writer.append(Config.SOFTWARE_CREDIT_LINK_TEXT);
        }
    }

    void printPrizes() throws IOException {

        final String race_name = (String) race.getConfig().get(Config.KEY_RACE_NAME_FOR_FILENAMES);
        final String year = (String) race.getConfig().get(Config.KEY_YEAR);

        try (final OutputStreamWriter writer = new OutputStreamWriter(getOutputStream(race_name, "prizes", year, Config.STANDARD_FILE_OPEN_OPTIONS))) {

            writer.append(getPrizesHeader());
            printPrizes(writer);
        }
    }

//    void printIndividualRaces() throws IOException {
//
//        for (int i = 1; i <= Integer.parseInt((String) race.getConfig().get(KEY_NUMBER_OF_RACES_IN_SERIES)); i++)
//            printIndividualRaceResults(i);
//    }
//
//    void printIndividualRaceResults(final int race_number) throws IOException {
//
//        final Race individual_race = ((TourRaceImpl) race.getSpecific()).getRaces().get(race_number - 1);
//        final String race_name = (String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES);
//        final String year = (String) race.getConfig().get(KEY_YEAR);
//
//        if (individual_race != null) {
//
//            final OutputStream race_stream = getOutputStream(race_name, STR."race\{race_number}", year);
//
//            try (final OutputStreamWriter writer = new OutputStreamWriter(race_stream)) {
//
//                for (final PrizeCategoryGroup group : race.getCategoryDetails().getPrizeCategoryGroups())
//                    printIndividualRaceResults(writer, individual_race, group.categories(), group.group_title());
//
//                writer.append(SOFTWARE_CREDIT_LINK_TEXT).append(LINE_SEPARATOR);
//            }
//        }
//    }
//
//    private void printIndividualRaceResults(final OutputStreamWriter writer, final Race individual_race, final List<PrizeCategory> prize_categories, final String sub_heading) throws IOException {
//
//        final List<RaceResult> category_results = individual_race.getResultsCalculator().getOverallResults(prize_categories);
//
//        new IndividualRaceResultPrinter(race, sub_heading, writer).print(category_results);
//    }

    /** Prints prizes, ordered by prize category groups. */
    private void printPrizes(final OutputStreamWriter writer) throws IOException {

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).                       // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory). // Ignore further categories once all prizes have been output.
            forEachOrdered(category -> printPrizes(writer, category));
    }

    /** Prints prizes within a given category. */
    private void printPrizes(final OutputStreamWriter writer, final PrizeCategory category) {

        try {
            writer.append(STR."""
                <p><strong>\{category.getLongName()}</strong></p>
                """);

            final List<RaceResult> category_prize_winners = race.getResultsCalculator().getPrizeWinners(category);
            new PrizeResultPrinter(race, writer).print(category_prize_winners);
        }
        // Called from lambda that can't throw checked exception.
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPrizesHeader() {

        final String header = ((TourRaceImpl) race.getSpecific()).getNumberOfRacesTakenPlace() < Integer.parseInt((String) race.getConfig().get(Config.KEY_NUMBER_OF_RACES_IN_SERIES)) ? "Current Standings" : "Prizes";
        return STR."<h4>\{header}</h4>\{Config.LINE_SEPARATOR}";
    }

    private OutputStream getOutputStream(final String race_name, final String output_type, final String year, final OpenOption... options) throws IOException {

        final Path path = race.getOutputDirectoryPath().resolve(STR."\{race_name}_\{output_type}_\{year}.\{Config.HTML_FILE_SUFFIX}");
        return Files.newOutputStream(path, options);
    }

    public String getResultsSubHeader(final String s) {
        return STR."""
            <p></p>
            <h4>\{s}</h4>
            """;
    }

    static void printResults(final OutputStreamWriter writer, final ResultPrinter printer, final Function<String, String> get_results_sub_header, final Race race) throws IOException {

        // Don't display category group headers if there is only one group.
        final boolean should_display_category_group_headers = race.getCategoryDetails().getPrizeCategoryGroups().size() > 1;

        boolean not_first_category_group = false;

        for (final PrizeCategoryGroup group : race.getCategoryDetails().getPrizeCategoryGroups()) {

            if (should_display_category_group_headers) {
                if (not_first_category_group)
                    writer.append(Config.LINE_SEPARATOR);
                writer.append(get_results_sub_header.apply(group.group_title()));
            }

            RaceResultsCalculator raceResults = race.getResultsCalculator();
            List<RaceResult> overallResults = raceResults.getOverallResults(group.categories());
            printer.print(overallResults);

            not_first_category_group = true;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OverallResultPrinter extends ResultPrinterHTML {

        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        protected List<String> getResultsColumnHeaders() {

            final List<String> common_headers = Arrays.asList("Pos", "Runner", "Category");

            final List<String> headers = new ArrayList<>(common_headers);

            headers.add("Club");

            final List<Race> races = ((TourRaceImpl) race.getSpecific()).getRaces();

            for (int i = 0; i < races.size(); i++)
                if (races.get(i) != null)
                    headers.add(STR."Race \{i + 1}");

            headers.add("Total");
//            headers.add("Completed?");

            return headers;
        }

        protected List<String> getResultsElements(final RaceResult r) {

            final List<String> elements = new ArrayList<>();

            final TourRaceResult result = (TourRaceResult) r;

            elements.add(result.position_string);
            elements.add(race.getNormalisation().htmlEncode(result.runner.name));
            elements.add(result.runner.category.getShortName());
            elements.add(result.runner.club);

            for (final Duration duration : result.times)
                elements.add(Config.renderDuration(duration, "-"));

            elements.add(renderDuration(result.duration(), "-"));

            return elements;
        }
    }

    private static final class PrizeResultPrinter extends ResultPrinterHTML {

        private PrizeResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {

            writer.append("<ul>").append(Config.LINE_SEPARATOR);
        }

        @Override
        public void printResultsFooter() throws IOException {

            writer.append("</ul>").append(Config.LINE_SEPARATOR).append(Config.LINE_SEPARATOR);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final TourRaceResult result = ((TourRaceResult) r);

            writer.append(STR."    <li>\{result.position_string} \{race.getNormalisation().htmlEncode(result.runner.name)} (\{result.runner.club}) \{format(result.duration())}</li>\n");
        }
    }

    private final class IndividualRaceResultPrinter extends ResultPrinterHTML {

        private final String sub_heading;

        private IndividualRaceResultPrinter(final Race race, final String sub_heading, final OutputStreamWriter writer) {

            super(race, writer);
            this.sub_heading = sub_heading;
        }

        @Override
        protected List<String> getResultsColumnHeaders() {

            return List.of("Pos", "No", "Runner", "Category", "Total");
        }

        @Override
        protected List<String> getResultsElements(final RaceResult r) {

            final List<String> elements = new ArrayList<>();

            final SingleRaceResult result = (SingleRaceResult) r;

            elements.add(result.position_string);
            elements.add(String.valueOf(result.entry.bib_number));
            elements.add(race.getNormalisation().htmlEncode(result.entry.participant.name));
            elements.add(result.entry.participant.category.getShortName());
            elements.add(renderDuration(result, Config.DNF_STRING));

            return elements;
        }

        @Override
        public void printResultsHeader() throws IOException {

            writer.append(Config.LINE_SEPARATOR).append(getResultsSubHeader(sub_heading));
            super.printResultsHeader();
        }
    }

}

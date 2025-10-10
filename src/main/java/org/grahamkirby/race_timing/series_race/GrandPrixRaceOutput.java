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


import org.grahamkirby.race_timing.common.*;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Config.*;

class GrandPrixRaceOutput extends SeriesRaceOutput {

    //////////////////////////////////////////////////////////////////////////////////////////////////

    void printResultsCSV() throws IOException {

        printResultsCSV(race, OverallResultPrinterCSV::new);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OverallResultPrinterCSV extends ResultPrinter {

        private OverallResultPrinterCSV(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {

            final GrandPrixRaceImpl race_impl = (GrandPrixRaceImpl) race.getSpecific();
            final String race_names = getConcatenatedRaceNames(race_impl.getRaces());

            final String race_categories_header = race_impl.race_categories.stream().
                map(GrandPrixRaceCategory::category_title).
                collect(Collectors.joining("?,")) + "?";

            writer.append("Pos,Runner,Category," + race_names + ",Total,Completed," + race_categories_header + LINE_SEPARATOR);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final GrandPrixRaceResult result = (GrandPrixRaceResult) r;
            final GrandPrixRaceImpl race_impl = (GrandPrixRaceImpl) race.getSpecific();
            final GrandPrixRaceResultsCalculator calculator = (GrandPrixRaceResultsCalculator) race.getResultsCalculator();
            final Runner runner = (Runner) result.getParticipant();

            writer.append(result.getPositionString() + "," + encode(runner.name) + "," + runner.category.getShortName() + ",");

            writer.append(
                race_impl.getRaces().stream().
                    filter(Objects::nonNull).
                    map(individual_race -> calculator.calculateRaceScore(individual_race, runner)).
                    map(OverallResultPrinterCSV::renderScore).
                    collect(Collectors.joining(","))
            );

            writer.append("," + result.totalScore() + "," + (result.hasCompletedSeries() ? "Y" : "N") + ",");

            writer.append(
                race_impl.race_categories.stream().
                    map(category -> result.hasCompletedRaceCategory(category) ? "Y" : "N").
                    collect(Collectors.joining(","))
            );

            writer.append(LINE_SEPARATOR);
        }

        private static String renderScore(final int score) {

            return score != 0 ? String.valueOf(score) : "-";
        }
    }

    @Override
    public void outputResults() throws IOException {

        printOverallResults();

        printPrizes();
        printNotes();
        printCombined();
    }

    @Override
    public void setRace(final Race race) {

        this.race = race;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void printOverallResults() throws IOException {

        printResultsCSV();
        printResultsHTML();

        for (final RaceResult result : race.getResultsCalculator().getOverallResults())
            if (result.getParticipant().category == null)
                race.appendToNotes("Runner " + result.getParticipantName() + " unknown category so omitted from overall results" + LINE_SEPARATOR);
    }

    private void printPrizes() throws IOException {

        printPrizesPDF(race);
        printPrizesHTML();
        printPrizesCSV();
    }

    private void printCombined() throws IOException {

        printCombinedHTML();
    }

    void printResultsHTML() throws IOException {

        printResults(race, GrandPrixOverallResultPrinterHTML::new);
    }

    void printCombinedHTML() throws IOException {

        printCombinedHTML(race, GrandPrixOverallResultPrinterHTML::new, GrandPrixPrizeResultPrinterHTML::new);
    }

    public void printPrizesHTML() throws IOException {

        printPrizesHTML(race, GrandPrixPrizeResultPrinterHTML::new);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class GrandPrixOverallResultPrinterHTML extends OverallResultPrinterHTML {

        private GrandPrixOverallResultPrinterHTML(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        protected List<String> getResultsColumnHeaders() {

            final List<String> common_headers = Arrays.asList("Pos", "Runner", "Category");
            final List<String> headers = new ArrayList<>(common_headers);

            // This traverses races in order of listing in config, sorted first by race type and then date.
            final List<Race> races = ((GrandPrixRaceImpl) race.getSpecific()).getRaces();

            for (final Race individual_race : races)
                // Check whether race has taken place at this point.
                if (individual_race != null)
                    headers.add((String) individual_race.getConfig().get(KEY_RACE_NAME_FOR_RESULTS));

            headers.add("Total");
            headers.add("Completed?");

            for (final GrandPrixRaceCategory category : ((GrandPrixRaceImpl) race.getSpecific()).race_categories)
                headers.add(category.category_title() + "?");

            return headers;
        }

        protected List<String> getResultsElements(final RaceResult r) {

            final List<String> elements = new ArrayList<>();

            final GrandPrixRaceResultsCalculator calculator = (GrandPrixRaceResultsCalculator) race.getResultsCalculator();
            final GrandPrixRaceResult result = (GrandPrixRaceResult) r;

            elements.add(result.getPositionString());
            elements.add(race.getNormalisation().htmlEncode(result.getParticipantName()));
            elements.add(result.getParticipant().category.getShortName());

            for (final Race individual_race : ((GrandPrixRaceImpl) race.getSpecific()).getRaces())
                if (individual_race != null) {
                    final int score = calculator.calculateRaceScore(individual_race, (Runner) result.getParticipant());
                    elements.add(renderScore(score));
                }

            elements.add(String.valueOf(result.totalScore()));
            elements.add(result.hasCompletedSeries() ? "Y" : "N");

            for (final GrandPrixRaceCategory category : ((GrandPrixRaceImpl) race.getSpecific()).race_categories)
                elements.add(result.hasCompletedRaceCategory(category) ? "Y" : "N");

            return elements;
        }

        private static String renderScore(final int score) {

            return score != 0 ? String.valueOf(score) : "-";
        }
    }

    public static final class GrandPrixPrizeResultPrinterHTML extends PrizeResultPrinterHTML {

        public GrandPrixPrizeResultPrinterHTML(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected String renderDetail(final RaceResult result) {
            return ((Runner) result.getParticipant()).club;
        }

        @Override
        protected String renderPerformance(final RaceResult result) {
            return String.valueOf(((GrandPrixRaceResult) result).totalScore());
        }
    }

    void printPrizesCSV() throws IOException {

        printPrizesCSV(race);
    }

    public static void printPrizesCSV(final Race race) throws IOException {

        final OutputStream stream = getOutputStream(race, "prizes", TEXT_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(getPrizesHeaderText(race));
            printPrizesText(writer, race);
        }
    }

    public static void printPrizesText(final OutputStreamWriter writer, final Race race) {

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).              // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory).          // Ignore further categories once all prizes have been output.
            forEachOrdered(category -> printPrizesText(writer, category, race));       // Print prizes in this category.
    }
}

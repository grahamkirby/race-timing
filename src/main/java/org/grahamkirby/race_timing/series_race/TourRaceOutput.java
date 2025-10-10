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
import org.grahamkirby.race_timing.relay_race.RelayRaceOutput;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.Normalisation.*;

class TourRaceOutput extends SeriesRaceOutput {


    void printResultsCSV() throws IOException {

        SeriesRaceOutput.printResultsCSV(race, OverallResultPrinterCSV::new);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OverallResultPrinterCSV extends ResultPrinter {

        private OverallResultPrinterCSV(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {

            final SeriesRace race_impl = (TourRaceImpl) race.getSpecific();
            final String race_names = SeriesRaceOutput.getConcatenatedRaceNames(race_impl.getRaces());

            writer.append("Pos,Runner,Club,Category," + race_names + ",Total" + LINE_SEPARATOR);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final TourRaceResult result = (TourRaceResult) r;
            final Runner runner = (Runner) result.getParticipant();

            writer.append(result.getPositionString() + "," + encode(runner.name) + "," + encode(runner.club) + "," + runner.category.getShortName() + ",");

            writer.append(
                result.times.stream().
                    map(time -> renderDuration(time, "-")).
                    collect(Collectors.joining(","))
            );

            writer.append("," + renderDuration(result, "-") + LINE_SEPARATOR);
        }
    }
    void printResultsHTML() throws IOException {

        RaceOutput.printResults(race, OverallResultPrinterHTML::new);
    }

    void printCombinedHTML() throws IOException {

        printCombinedHTML(race, OverallResultPrinterHTML::new, PrizeResultPrinterHTML::new);
    }

    public void printPrizesHTML() throws IOException {

        printPrizesHTML(race, PrizeResultPrinterHTML::new);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OverallResultPrinterHTML extends org.grahamkirby.race_timing.common.OverallResultPrinterHTML {

        private OverallResultPrinterHTML(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected List<String> getResultsColumnHeaders() {

            final List<String> common_headers = Arrays.asList("Pos", "Runner", "Category");
            final List<String> headers = new ArrayList<>(common_headers);

            headers.add("Club");

            final List<Race> races = ((SeriesRace) race.getSpecific()).getRaces();

            for (int i = 0; i < races.size(); i++)
                if (races.get(i) != null)
                    headers.add("Race " + (i + 1));

            headers.add("Total");

            return headers;
        }

        @Override
        protected List<String> getResultsElements(final RaceResult r) {

            final List<String> elements = new ArrayList<>();

            final TourRaceResult result = (TourRaceResult) r;

            final Runner runner = (Runner) result.getParticipant();

            elements.add(result.getPositionString());
            elements.add(race.getNormalisation().htmlEncode(runner.name));
            elements.add(runner.category.getShortName());
            elements.add(runner.club);

            for (final Duration duration : result.times)
                elements.add(renderDuration(duration, "-"));

            elements.add(renderDuration(result, "-"));

            return elements;
        }
    }

    public static final class PrizeResultPrinterHTML extends org.grahamkirby.race_timing.common.PrizeResultPrinterHTML {

        public PrizeResultPrinterHTML(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected String renderDetail(final RaceResult result) {
            return ((Runner) result.getParticipant()).club;
        }

        @Override
        protected String renderPerformance(final RaceResult result) {
            return renderDuration((RaceResultWithDuration) result, "-");
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

//        output_CSV = new TourRaceOutput(race);
//        output_HTML = new TourRaceOutputHTML(race);
//        output_text = new SeriesRaceOutputText(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void printOverallResults() throws IOException {

        printResultsCSV();
        printResultsHTML();

        for (final RaceResult result : race.getResultsCalculator().getOverallResults())
            if (result.getCategory() == null)
                race.appendToNotes(LINE_SEPARATOR + "Runner " + result.getParticipantName() + " unknown category so omitted from overall results" + LINE_SEPARATOR);
    }

    private void printPrizes() throws IOException {

        RaceOutput.printPrizesPDF(race);
        printPrizesHTML();
        printPrizesCSV();
    }

    private void printCombined() throws IOException {

        printCombinedHTML();
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

    /** Prints out the words converted to title case, and any other processing notes. */
    void printNotes() throws IOException {

        printNotes(race);
    }

    public static void printNotes(final Race race) throws IOException {

        final String converted_words = race.getNormalisation().getNonTitleCaseWords();

        if (!converted_words.isEmpty())
            race.appendToNotes("Converted to title case: " + converted_words);

        final OutputStream stream = getOutputStream(race, "processing_notes", TEXT_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            writer.append(race.getNotes());
        }
    }

    public static String getPrizesHeaderText(final Race race) {

        final String header = race.getConfig().get(KEY_RACE_NAME_FOR_RESULTS) + " Results " + race.getConfig().get(KEY_YEAR);
        return header + LINE_SEPARATOR + "=".repeat(header.length()) + LINE_SEPARATOR + LINE_SEPARATOR;
    }

    public static void printPrizesText(final OutputStreamWriter writer, final Race race) {

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).              // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory).          // Ignore further categories once all prizes have been output.
            forEachOrdered(category -> RelayRaceOutput.printPrizesText(writer, category, race));       // Print prizes in this category.
    }
}

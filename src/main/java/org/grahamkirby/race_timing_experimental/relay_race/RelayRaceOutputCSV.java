/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (graham.kirby@st-andrews.ac.uk)
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
package org.grahamkirby.race_timing_experimental.relay_race;


import org.grahamkirby.race_timing.common.Team;
import org.grahamkirby.race_timing.common.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing_experimental.common.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.List;

import static org.grahamkirby.race_timing.common.Normalisation.format;
import static org.grahamkirby.race_timing.common.Race.KEY_RACE_NAME_FOR_FILENAMES;
import static org.grahamkirby.race_timing.common.Race.LINE_SEPARATOR;
import static org.grahamkirby.race_timing_experimental.common.Config.KEY_YEAR;

public class RelayRaceOutputCSV {

    /** Displayed in results for runners that did not complete the course. */
    public static final String DNF_STRING = "DNF";
    private static final OpenOption[] STANDARD_FILE_OPEN_OPTIONS = {StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE};

    private static final String OVERALL_RESULTS_HEADER = "Pos,No,Team,Category,";
    private final Race race;

    RelayRaceOutputCSV(final Race race) {
        this.race = race;
    }

    public String getResultsHeader() {
        return STR."\{OVERALL_RESULTS_HEADER}Total\{LINE_SEPARATOR}";
    }

    protected ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer) {
        return new OverallResultPrinter(race, writer);
    }

    public void printResults() throws IOException {

        final OutputStream stream = getOutputStream((String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES), "overall", (String) race.getConfig().get(KEY_YEAR));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(getResultsHeader());
            printResults(writer, getOverallResultPrinter(writer));
        }
    }

    void printDetailedResults() throws IOException {

        try (final OutputStreamWriter writer = new OutputStreamWriter(getOutputStream((String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES), "detailed", (String) race.getConfig().get(KEY_YEAR)))) {

            printDetailedResultsHeader(writer);
            printDetailedResults(writer);
        }
    }

    private void printDetailedResultsHeader(final OutputStreamWriter writer) throws IOException {

        final int number_of_legs = ((RelayRaceImpl)race.getSpecific()).getNumberOfLegs();

        writer.append(OVERALL_RESULTS_HEADER);

        for (int leg_number = 1; leg_number <= number_of_legs; leg_number++) {

            writer.append(STR."Runners \{leg_number},Leg \{leg_number},");
            if (leg_number < number_of_legs) writer.append(STR."Split \{leg_number},");
        }

        writer.append("Total").append(LINE_SEPARATOR);
    }

    private void printDetailedResults(final OutputStreamWriter writer) throws IOException {

        final ResultPrinter printer = new DetailedResultPrinter(race, writer);

        for (final PrizeCategoryGroup group : race.getCategoryDetails().getPrizeCategoryGroups())
            printer.print(race.getResultsCalculator().getOverallResults(group.categories()));
    }

    void printLegResults() throws IOException {

        for (int leg = 1; leg <= ((RelayRaceImpl) race.getSpecific()).getNumberOfLegs(); leg++)
            printLegResults(leg);
    }

    private void printLegResults(final int leg) throws IOException {

        try (final OutputStreamWriter writer = new OutputStreamWriter(getOutputStream((String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES), STR."leg_\{leg}", (String) race.getConfig().get(KEY_YEAR)))) {

            printLegResults(writer, leg);
        }
    }

    private void printLegResults(final OutputStreamWriter writer, final int leg) throws IOException {

        final List<LegResult> leg_results = ((RelayRaceImpl) race.getSpecific()).getLegResults(leg);

        new LegResultPrinter(race, writer, leg).print(leg_results);
    }


    /**
     * Constructs an output stream for writing to a file in the project output directory with name constructed from the given components.
     * The file extension is determined by getFileSuffix().
     * The file is created if it does not already exist, and overwritten if it does.
     * Example file name: "balmullo_prizes_2023.html".
     *
     * @param race_name the name of the race in format suitable for inclusion with file name
     * @param output_type the type of output file e.g. "overall", "prizes" etc.
     * @param year the year of the race
     * @return an output stream for the file
     * @throws IOException if an I/O error occurs
     */
    protected OutputStream getOutputStream(final String race_name, final String output_type, final String year) throws IOException {

        return getOutputStream(race_name, output_type, year, STANDARD_FILE_OPEN_OPTIONS);
    }

    /** As {@link #getOutputStream(String, String, String)} with specified file creation options. */
    protected OutputStream getOutputStream(final String race_name, final String output_type, final String year, final OpenOption... options) throws IOException {

        return Files.newOutputStream(getOutputFilePath(race_name, output_type, year), options);
    }

    /**
     * Constructs a path for a file in the project output directory with name constructed from the given components.
     * The file extension is determined by getFileSuffix().
     * Example file name: "balmullo_prizes_2023.html".
     *
     * @param race_name the name of the race in format suitable for inclusion with file name
     * @param output_type the type of output file e.g. "overall", "prizes" etc.
     * @param year the year of the race
     * @return the path for the file
     */
    Path getOutputFilePath(final String race_name, final String output_type, final String year) {

        return race.getOutputDirectoryPath().resolve(STR."\{race_name}_\{output_type}_\{year}.csv");
    }

    /** Prints results using a specified printer, ordered by prize category groups. */
    protected void printResults(final OutputStreamWriter writer, final ResultPrinter printer) throws IOException {

        // Don't display category group headers if there is only one group.
        final boolean should_display_category_group_headers = race.getCategoryDetails().getPrizeCategoryGroups().size() > 1;

        boolean not_first_category_group = false;

        for (final PrizeCategoryGroup group : race.getCategoryDetails().getPrizeCategoryGroups()) {

            if (should_display_category_group_headers) {
                if (not_first_category_group)
                    writer.append(System.lineSeparator());
                writer.append(getResultsSubHeader(group.group_title()));
            }

            RaceResultsCalculator raceResults = race.getResultsCalculator();
            List<RaceResult> overallResults = raceResults.getOverallResults(group.categories());
            printer.print(overallResults);

            not_first_category_group = true;
        }
    }

    protected String getResultsSubHeader(final String s) {
        return "";
    }

    /** Encodes a single value by surrounding with quotes if it contains a comma. */
    public static String encode(final String s) {
        return s.contains(",") ? STR."\"\{s}\"" : s;
    }

    public static String renderDuration(final Duration duration, final String alternative) {

        return duration != null ? format(duration) : alternative;
    }

    public static String renderDuration(final RaceResult result, final String alternative) {

        if (!result.canComplete()) return alternative;

        return format(((SingleRaceResult)result).duration());
    }

    public static String renderDuration(final RaceResult result) {
        return renderDuration(result, "");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OverallResultPrinter extends ResultPrinter {

        // TODO investigate Files.write.
        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        public void printResult(final RaceResult r) throws IOException {

            RelayRaceResult result = (RelayRaceResult) r;
            writer.append(STR."\{result.position_string},\{result.entry.bib_number},\{encode(result.entry.participant.name)},\{result.entry.participant.category.getShortName()},\{renderDuration(result, DNF_STRING)}\n");
        }
    }

    private static final class DetailedResultPrinter extends ResultPrinter {

        private DetailedResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final RelayRaceResult result = (RelayRaceResult) r;

            writer.append(STR."\{result.position_string},\{result.entry.bib_number},\{encode(result.entry.participant.name)},\{result.entry.participant.category.getLongName()},");

            final List<String> leg_strings = ((RelayRaceImpl)race.getSpecific()).getLegDetails(result).stream().
                map(RelayRaceOutputCSV::encode).toList();

            writer.append(String.join(",", leg_strings));
            writer.append(LINE_SEPARATOR);
        }
    }

    private static final class LegResultPrinter extends ResultPrinter {

        final int leg;

        private LegResultPrinter(final Race race, final OutputStreamWriter writer, final int leg) {

            super(race, writer);
            this.leg = leg;
        }

        @Override
        public void printResultsHeader() throws IOException {

            final String plural = ((RelayRaceImpl) race.getSpecific()).getPairedLegs().get(leg - 1) ? "s" : "";
            writer.append(STR."Pos,Runner\{plural},Time\n");
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final LegResult result = (LegResult) r;
            final String runner_names = encode(((Team) result.entry.participant).runner_names.get(result.leg_number - 1));

            writer.append(STR."\{result.position_string},\{runner_names},\{renderDuration(result, DNF_STRING)}\n");
        }
    }
}

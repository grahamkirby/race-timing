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
package org.grahamkirby.race_timing.relay_race;


import org.grahamkirby.race_timing.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing.common.Config;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.ResultPrinter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;

import static org.grahamkirby.race_timing.common.Config.*;

public class RelayRaceOutputCSV {

    private static final String OVERALL_RESULTS_HEADER = "Pos,No,Team,Category,";
    private final Race race;

    RelayRaceOutputCSV(final Race race) {
        this.race = race;
    }

    void printResults() throws IOException {

        final String race_name = (String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES);
        final String year = (String) race.getConfig().get(KEY_YEAR);

        final OutputStream stream = Files.newOutputStream(race.getOutputDirectoryPath().resolve(race_name + "_overall_" + year + "." + CSV_FILE_SUFFIX), STANDARD_FILE_OPEN_OPTIONS);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(OVERALL_RESULTS_HEADER + "Total" + LINE_SEPARATOR);
            printResults(writer, new OverallResultPrinter(race, writer));
        }
    }

    void printDetailedResults() throws IOException {

        final String race_name = (String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES);
        final String year = (String) race.getConfig().get(KEY_YEAR);

        try (final OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(race.getOutputDirectoryPath().resolve(race_name + "_detailed_" + year + "." + CSV_FILE_SUFFIX), STANDARD_FILE_OPEN_OPTIONS))) {

            printDetailedResultsHeader(writer);
            printDetailedResults(writer);
        }
    }

    void printLegResults() throws IOException {

        for (int leg = 1; leg <= ((RelayRaceImpl) race.getSpecific()).getNumberOfLegs(); leg++)
            printLegResults(leg);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void printDetailedResultsHeader(final OutputStreamWriter writer) throws IOException {

        final int number_of_legs = ((RelayRaceImpl)race.getSpecific()).getNumberOfLegs();

        writer.append(OVERALL_RESULTS_HEADER);

        for (int leg_number = 1; leg_number <= number_of_legs; leg_number++) {

            writer.append("Runners " + leg_number + ",Leg " + leg_number + ",");
            if (leg_number < number_of_legs) writer.append("Split " + leg_number + ",");
        }

        writer.append("Total").append(LINE_SEPARATOR);
    }

    private void printDetailedResults(final OutputStreamWriter writer) throws IOException {

        final ResultPrinter printer = new DetailedResultPrinter(race, writer);

        for (final PrizeCategoryGroup group : race.getCategoryDetails().getPrizeCategoryGroups())
            printer.print(race.getResultsCalculator().getOverallResults(group.categories()));
    }

    private void printLegResults(final int leg) throws IOException {

        final String race_name = (String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES);
        final String output_type = "leg_" + leg;
        final String year = (String) race.getConfig().get(KEY_YEAR);

        try (final OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(race.getOutputDirectoryPath().resolve(race_name + "_" + output_type + "_" + year + "." + CSV_FILE_SUFFIX), STANDARD_FILE_OPEN_OPTIONS))) {

            final List<LegResult> leg_results = ((RelayRaceImpl) race.getSpecific()).getLegResults(leg);
            new LegResultPrinter(race, writer, leg).print(leg_results);
        }
    }

    /** Prints results using a specified printer, ordered by prize category groups. */
    private void printResults(final OutputStreamWriter writer, final ResultPrinter printer) throws IOException {

        // Don't display category group headers if there is only one group.
        final boolean should_display_category_group_headers = race.getCategoryDetails().getPrizeCategoryGroups().size() > 1;

        boolean not_first_category_group = false;

        for (final PrizeCategoryGroup group : race.getCategoryDetails().getPrizeCategoryGroups()) {

            if (should_display_category_group_headers && not_first_category_group)
                writer.append(LINE_SEPARATOR);

            printer.print(race.getResultsCalculator().getOverallResults(group.categories()));

            not_first_category_group = true;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OverallResultPrinter extends ResultPrinter {

        // TODO investigate Files.write.
        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        public void printResult(final RaceResult r) throws IOException {

            RelayRaceResult result = (RelayRaceResult) r;
            writer.append(result.getPositionString() + "," + result.bib_number + "," + encode(result.getParticipantName()) + "," + result.getParticipant().category.getShortName() + "," + renderDuration(result, DNF_STRING) + LINE_SEPARATOR);
//            writer.append(result.getPositionString() + "," + result.entry.bib_number + "," + encode(result.entry.participant.name) + "," + result.entry.participant.category.getShortName() + "," + renderDuration(result, DNF_STRING) + LINE_SEPARATOR);
        }
    }

    private static final class DetailedResultPrinter extends ResultPrinter {

        private DetailedResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final RelayRaceResult result = (RelayRaceResult) r;

            writer.append(result.getPositionString() + "," + result.bib_number + "," + encode(result.getParticipantName()) + "," + result.getParticipant().category.getLongName() + ",");
//            writer.append(result.getPositionString() + "," + result.entry.bib_number + "," + encode(result.entry.participant.name) + "," + result.entry.participant.category.getLongName() + ",");

            final List<String> leg_strings = ((RelayRaceImpl) race.getSpecific()).getLegDetails(result).stream().
                map(Config::encode).toList();

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
            writer.append("Pos,Runner" + plural + ",Time" + LINE_SEPARATOR);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final LegResult result = (LegResult) r;
            final String runner_names = encode(((Team) result.getParticipant()).runner_names.get(result.leg_number - 1));
//            final String runner_names = encode(((Team) result.entry.participant).runner_names.get(result.leg_number - 1));

            writer.append(result.getPositionString() + "," + runner_names + "," + renderDuration(result, DNF_STRING) + LINE_SEPARATOR);
        }
    }
}

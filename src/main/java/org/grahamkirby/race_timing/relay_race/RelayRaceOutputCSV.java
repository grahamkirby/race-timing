/*
 * Copyright 2024 Graham Kirby:
 * <https://github.com/grahamkirby/race-timing>
 *
 * This file is part of the module race-timing.
 *
 * race-timing is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * race-timing is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with race-timing. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.relay_race;

import org.grahamkirby.race_timing.common.CompletionStatus;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing.common.output.RaceOutputCSV;
import org.grahamkirby.race_timing.common.output.ResultPrinter;
import org.grahamkirby.race_timing.common.output.ResultPrinterCSV;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;

import static org.grahamkirby.race_timing.common.Normalisation.format;
import static org.grahamkirby.race_timing.common.Race.SUFFIX_CSV;

public class RelayRaceOutputCSV extends RaceOutputCSV {

    private String detailed_results_filename;

    private static final String OVERALL_RESULTS_HEADER = "Pos,No,Team,Category,";

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public RelayRaceOutputCSV(final Race race) {
        
        super(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void constructFilePaths() {

        super.constructFilePaths();
        detailed_results_filename = race_name_for_filenames + "_detailed_" + year;
    }

    @Override
    public String getResultsHeader() {
        return OVERALL_RESULTS_HEADER + "Total\n";
    }

    @Override
    protected ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer) {
        return new OverallResultPrinter(race, writer);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void printDetailedResults() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(detailed_results_filename + SUFFIX_CSV));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            printDetailedResultsHeader(writer);
            printDetailedResults(writer);
        }
    }

    private void printDetailedResultsHeader(final OutputStreamWriter writer) throws IOException {

        writer.append(OVERALL_RESULTS_HEADER);

        for (int leg_number = 1; leg_number <= ((RelayRace)race).number_of_legs; leg_number++) {

            writer.append("Runners ").append(String.valueOf(leg_number)).append(",Leg ").append(String.valueOf(leg_number)).append(",");
            if (leg_number < ((RelayRace)race).number_of_legs) writer.append("Split ").append(String.valueOf(leg_number)).append(",");
        }

        writer.append("Total\n");
    }

    private void printDetailedResults(final OutputStreamWriter writer) throws IOException {

        final ResultPrinter printer = new DetailedResultPrinter(race, writer);

        for (final PrizeCategoryGroup group : race.prize_category_groups) {

            final List<RaceResult> results = race.getOverallResults(group.categories());
            printer.print(results, false);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void printLegResults() throws IOException {

        for (int leg = 1; leg <= ((RelayRace)race).number_of_legs; leg++)
            printLegResults(leg);
    }

    private void printLegResults(final int leg) throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(race_name_for_filenames + "_leg_" + leg + "_" + year + ".csv"));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            printLegResults(writer, leg);
        }
    }

    private void printLegResults(final OutputStreamWriter writer, final int leg) throws IOException {

        final List<LegResult> leg_results = ((RelayRace) race).getLegResults(leg);

        new LegResultPrinter(race, writer, leg).print(leg_results, false);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    // Prize results not printed to text file.
    @Override
    protected ResultPrinter getPrizeResultPrinter(final OutputStreamWriter writer) { throw new UnsupportedOperationException(); }

    private static class OverallResultPrinter extends ResultPrinterCSV {

        public OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final RelayRaceResult result = (RelayRaceResult) r;
            writer.append(STR."\{result.shouldDisplayPosition() ? result.position_string : ""},\{result.entry.bib_number},\{encode(result.entry.team.name())},\{result.entry.team.category().getShortName()},\{result.getCompletionStatus() == CompletionStatus.DNF ? "DNF" : format(result.duration())}\n");
        }
    }

    private static class LegResultPrinter extends ResultPrinterCSV {

        final int leg;

        public LegResultPrinter(final Race race, final OutputStreamWriter writer, final int leg) {

            super(race, writer);
            this.leg = leg;
        }

        @Override
        public void printResultsHeader() throws IOException {

            final String plural = ((RelayRace) race).paired_legs.contains(leg) ? "s" : "";
            writer.append(STR."Pos,Runner\{plural},Time\n");
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final LegResult result = (LegResult) r;
            writer.append(STR."\{result.position_string},\{encode(result.entry.team.runner_names().get(result.leg_number - 1))},\{format(result.duration())}\n");
        }
    }

    private static class DetailedResultPrinter extends ResultPrinterCSV {

        public DetailedResultPrinter(Race race, OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final RelayRace relay_race = (RelayRace) race;
            final RelayRaceResult result = (RelayRaceResult) r;

            writer.append(STR."\{result.shouldDisplayPosition() ? result.position_string : ""},\{result.entry.bib_number},\{encode(result.entry.team.name())},\{result.entry.team.category().getLongName()},");

            boolean any_previous_leg_dnf = false;

            for (int leg = 1; leg <= relay_race.number_of_legs; leg++) {

                final LegResult leg_result = result.leg_results.get(leg - 1);
                final boolean completed = leg_result.getCompletionStatus() == CompletionStatus.COMPLETED;

                final String leg_runner_names = leg_result.entry.team.runner_names().get(leg - 1);
                final String leg_mass_start_annotation = relay_race.getMassStartAnnotation(leg_result, leg);
                final String leg_time = completed ? format(leg_result.duration()) : DNF_STRING;
                final String split_time = completed && !any_previous_leg_dnf ? format(relay_race.sumDurationsUpToLeg(result.leg_results, leg)) : DNF_STRING;

                writer.append(STR."\{encode(leg_runner_names)}\{leg_mass_start_annotation},\{leg_time},\{split_time}");

                if (leg < relay_race.number_of_legs) writer.append(",");
                if (!completed) any_previous_leg_dnf = true;
            }

            writer.append("\n");
        }
    }
}

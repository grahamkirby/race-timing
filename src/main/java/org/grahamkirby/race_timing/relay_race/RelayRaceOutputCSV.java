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

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Team;
import org.grahamkirby.race_timing.common.output.OverallResultPrinterCSV;
import org.grahamkirby.race_timing.common.output.RaceOutputCSV;
import org.grahamkirby.race_timing.common.output.ResultPrinter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.grahamkirby.race_timing.common.Normalisation.format;
import static org.grahamkirby.race_timing.common.Race.SUFFIX_CSV;

public class RelayRaceOutputCSV extends RaceOutputCSV {

    private String detailed_results_filename;

    private static final String OVERALL_RESULTS_HEADER = "Pos,No,Team,Category,";

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public RelayRaceOutputCSV(final Race race) {
        
        super(race);
        constructFilePaths();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public void printDetailedResults() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(detailed_results_filename + SUFFIX_CSV));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            printDetailedResultsHeader(writer);
            printDetailedResults(writer);
        }
    }

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

    protected void printLegResults() throws IOException {

        for (int leg = 1; leg <= ((RelayRace)race).number_of_legs; leg++)
            printLegResults(leg);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void printLegResults(final int leg_number) throws IOException {

        final Path leg_results_csv_path = output_directory_path.resolve(race_name_for_filenames + "_leg_" + leg_number + "_" + year + ".csv");

        try (final OutputStreamWriter csv_writer = new OutputStreamWriter(Files.newOutputStream(leg_results_csv_path))) {

            final List<LegResult> leg_results = ((RelayRace)race).getLegResults(leg_number);

            // Deal with dead heats in legs after the first.
            setPositionStrings(leg_results, leg_number > 1);

            printLegResultsHeader(csv_writer, leg_number);
            printLegResults(csv_writer, leg_results);
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

        for (int result_index = 0; result_index < race.getOverallResults().size(); result_index++)
            printDetailedResult(writer, result_index);
    }

    private void printDetailedResult(final OutputStreamWriter writer, final int result_index) throws IOException {

        final RelayRaceResult result = (RelayRaceResult) race.getOverallResults().get(result_index);

        if (result.shouldDisplayPosition())
            writer.append(result.position_string);

        writer.append(",");
        writer.append(String.valueOf(result.entry.bib_number)).append(",");
        writer.append(result.entry.team.name()).append(",");
        writer.append(result.entry.team.category().getLongName()).append(",");

        printLegDetails(writer, result, result.entry.team);

        writer.append("\n");
    }

    private void printLegDetails(final OutputStreamWriter writer, final RelayRaceResult result, final Team team) throws IOException {

        boolean any_previous_leg_dnf = false;

        for (int leg_number = 1; leg_number <= ((RelayRace)race).number_of_legs; leg_number++) {

            final LegResult leg_result = result.leg_results.get(leg_number - 1);

            writer.append(team.runner_names().get(leg_number-1));
            ((RelayRace)race).addMassStartAnnotation(writer, leg_result, leg_number);

            writer.append(",");
            writer.append(leg_result.DNF ? DNF_STRING : format(leg_result.duration())).append(",");
            writer.append(leg_result.DNF || any_previous_leg_dnf ? DNF_STRING : format(((RelayRace)race).sumDurationsUpToLeg(result.leg_results, leg_number)));

            if (leg_number < ((RelayRace)race).number_of_legs) writer.append(",");
            if (leg_result.DNF) any_previous_leg_dnf = true;
        }
    }

    private void printLegResultsHeader(final OutputStreamWriter writer, final int leg_number) throws IOException {

        writer.append("Pos,Runner");
        if (((RelayRace)race).paired_legs.get(leg_number-1)) writer.append("s");
        writer.append(",Time\n");
    }

    private void printLegResults(final OutputStreamWriter writer, final List<LegResult> leg_results) throws IOException {

        for (final LegResult result : leg_results)
            printLegResult(writer, result);
    }

    private void printLegResult(final OutputStreamWriter writer, final LegResult result) throws IOException {

        if (!result.DNF) {
            writer.append(result.position_string).append(",");
            writer.append(result.entry.team.runner_names().get(result.leg_number - 1)).append(",");
            writer.append(format(result.duration())).append("\n");
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    // Prize results not printed to text file.
    @Override
    protected ResultPrinter getPrizeResultPrinter(final OutputStreamWriter writer) { throw new UnsupportedOperationException(); }

    private static class OverallResultPrinter extends OverallResultPrinterCSV {

        public OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final RelayRaceResult result = (RelayRaceResult) r;

            if (result.shouldDisplayPosition())
                writer.append(result.position_string);

            writer.append(",").
                    append(String.valueOf(result.entry.bib_number)).append(",").
                    append(result.entry.team.name()).append(",").
//                    append(result.entry.team.category().getLongName()).append(",").
                    append(result.entry.team.category().getShortName()).append(",").
                    append(result.dnf() ? "DNF" : format(result.duration())).append("\n");
        }
    }
}

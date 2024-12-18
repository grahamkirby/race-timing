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

import org.grahamkirby.race_timing.common.Normalisation;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.common.output.ResultPrinterText;
import org.grahamkirby.race_timing.common.output.RaceOutputText;
import org.grahamkirby.race_timing.common.output.ResultPrinter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.grahamkirby.race_timing.common.Normalisation.format;
import static org.grahamkirby.race_timing.common.Race.UNKNOWN_BIB_NUMBER;

public class RelayRaceOutputText extends RaceOutputText {

    private String collated_times_filename;

    RelayRaceOutputText(final RelayRace results) {

        super(results);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void constructFilePaths() {

        super.constructFilePaths();
        collated_times_filename = "times_collated";
    }

    void printCollatedResults() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(collated_times_filename + ".txt"));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            final Map<Integer, Integer> legs_finished_per_team = countLegsFinishedPerTeam();

            printResults(writer, legs_finished_per_team);

            final List<Integer> bib_numbers_with_missing_times = getBibNumbersWithMissingTimes(legs_finished_per_team);
            final List<Duration> times_with_missing_bib_numbers = getTimesWithMissingBibNumbers();

            final boolean discrepancies_exist = !bib_numbers_with_missing_times.isEmpty() || !times_with_missing_bib_numbers.isEmpty();

            if (discrepancies_exist)
                race.getNotes().append("""
                
                Discrepancies:
                -------------
                """);

            printBibNumbersWithMissingTimes(bib_numbers_with_missing_times);
            printTimesWithMissingBibNumbers(times_with_missing_bib_numbers);

            if (discrepancies_exist)
                race.getNotes().append("""
                
                
                """);
        }
    }

    private void printBibNumbersWithMissingTimes(final List<Integer> bib_numbers_with_missing_times) {

        if (!bib_numbers_with_missing_times.isEmpty()) {

            race.getNotes().append("""
                
                Bib numbers with missing times:\s""");

            race.getNotes().append(
                bib_numbers_with_missing_times.stream().
                map(String::valueOf).
                reduce((i1, i2) -> STR."\{i1}, \{i2}").
                orElse(""));
        }
    }

    private void printTimesWithMissingBibNumbers(final List<Duration> times_with_missing_bib_numbers) {

        if (!times_with_missing_bib_numbers.isEmpty()) {

            race.getNotes().append("""
                
                Times with missing bib numbers:
                
                """);

            race.getNotes().append(
                times_with_missing_bib_numbers.stream().
                map(Normalisation::format).
                reduce((i1, i2) -> STR."\{i1}\n\{i2}").
                orElse(""));
        }
    }

    private void printResults(final OutputStreamWriter writer, final Map<Integer, Integer> legs_finished_per_team) throws IOException {

        for (final RawResult result : ((RelayRace)race).getRawResults()) {

            final int legs_already_finished = legs_finished_per_team.get(result.getBibNumber()) - 1;
            printResult(writer, (RelayRaceRawResult) result, legs_already_finished);
        }
    }

    private Map<Integer, Integer> countLegsFinishedPerTeam() {

        final Map<Integer, Integer> legs_finished_map = new HashMap<>();

        for (final RawResult result : ((RelayRace)race).getRawResults())
            legs_finished_map.merge(result.getBibNumber(), 1, Integer::sum);

        return legs_finished_map;
    }

    private List<Duration> getTimesWithMissingBibNumbers() {

        final List<Duration> times_with_missing_bib_numbers = new ArrayList<>();

        for (final RawResult raw_result : ((RelayRace)race).getRawResults()) {

            if (raw_result.getBibNumber() == UNKNOWN_BIB_NUMBER)
                times_with_missing_bib_numbers.add(raw_result.getRecordedFinishTime());
        }

        return times_with_missing_bib_numbers;
    }

    private List<Integer> getBibNumbersWithMissingTimes(final Map<Integer, Integer> leg_finished_count) {

        return ((RelayRace)race).entries.stream().
            flatMap(entry -> {
                final int bib_number = entry.bib_number;
                final int number_of_legs_unfinished = ((RelayRace) race).getNumberOfLegs() - leg_finished_count.getOrDefault(bib_number, 0);

                return Stream.generate(() -> bib_number).limit(number_of_legs_unfinished);
            }).
            sorted().
            toList();
    }

    private void printResult(final OutputStreamWriter writer, final RelayRaceRawResult raw_result, final int legs_already_finished) throws IOException {

        printBibNumberAndTime(writer, raw_result);
        printLegNumber(writer, raw_result, legs_already_finished);
        printComment(writer, raw_result);
    }

    private void printBibNumberAndTime(final OutputStreamWriter writer, final RawResult raw_result) throws IOException {

        final int bib_number = raw_result.getBibNumber();

        writer.append(bib_number != UNKNOWN_BIB_NUMBER ? String.valueOf(bib_number) : "?").
                append("\t").
                append(raw_result.getRecordedFinishTime() != null ? format(raw_result.getRecordedFinishTime()) : "?");
    }

    private void printLegNumber(final OutputStreamWriter writer, final RelayRaceRawResult raw_result, final int legs_already_finished) throws IOException {

        if (raw_result.getLegNumber() > 0) {

            writer.append("\t").append(String.valueOf(raw_result.getLegNumber()));

            if (legs_already_finished >= raw_result.getLegNumber())
                raw_result.appendComment(STR."Leg \{raw_result.getLegNumber()} finisher was runner \{legs_already_finished + 1} to finish for team.");
        }
    }

    private void printComment(final OutputStreamWriter writer, final RelayRaceRawResult raw_result) throws IOException {

        if (!raw_result.getComment().isEmpty()) {

            if (raw_result.getLegNumber() == 0) writer.append("\t");
            writer.append("\t").append(Race.COMMENT_SYMBOL).append(" ").append(raw_result.getComment());
        }

        writer.append("\n");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected ResultPrinter getPrizeResultPrinter(final OutputStreamWriter writer) {
        return new PrizeResultPrinter(race, writer);
    }

    private static class PrizeResultPrinter extends ResultPrinterText {

        public PrizeResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final RelayRaceResult result = ((RelayRaceResult)r);

            writer.append(STR."\{result.position_string}: \{result.entry.team.name()} (\{result.entry.team.category().getLongName()}) \{format(result.duration())}\n");
        }
    }
}

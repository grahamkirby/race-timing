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
import org.grahamkirby.race_timing.common.RaceEntry;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.common.categories.Category;
import org.grahamkirby.race_timing.common.output.RaceOutputText;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.grahamkirby.race_timing.common.Normalisation.format;

public class RelayRaceOutputText extends RaceOutputText {

    private String collated_times_filename;

    public RelayRaceOutputText(final RelayRace results) {

        super(results);
        constructFilePaths();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public void printCollatedResults() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(collated_times_filename + ".txt"));

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            final Map<Integer, Integer> legs_finished_per_team = countLegsFinishedPerTeam();

            printResults(writer, legs_finished_per_team);

            final List<Integer> bib_numbers_with_missing_times = getBibNumbersWithMissingTimes(legs_finished_per_team);
            final List<Duration> times_with_missing_bib_numbers = getTimesWithMissingBibNumbers();

            if (!bib_numbers_with_missing_times.isEmpty())
                printDiscrepancies(writer, bib_numbers_with_missing_times, times_with_missing_bib_numbers);
        }
    }

    @Override
    public void printPrizes(final OutputStreamWriter writer, final Category category) throws IOException {

        final String header = "Category: " + category.getLongName();
        final List<RaceResult> category_prize_winners = race.prize_winners.get(category);

        writer.append(header).append("\n");
        writer.append("-".repeat(header.length())).append("\n\n");

        if (category_prize_winners.isEmpty())
            writer.append("No results\n");
        else
            printPrizes(writer, category_prize_winners);

        writer.append("\n\n");
    }

    @Override
    protected void constructFilePaths() {

        super.constructFilePaths();
        collated_times_filename = "times_collated";
    }

    @Override
    protected void printPrizes(final OutputStreamWriter writer, final List<RaceResult> category_prize_winners) throws IOException {

        int position = 1;
        for (final RaceResult r : category_prize_winners) {

            final RelayRaceResult result = ((RelayRaceResult)r);

            // No dead heats in overall results since determined by ordering at finish.
            writer.append(String.valueOf(position++)).append(": ").
                    append(result.entry.team.name()).append(" (").
                    append(result.entry.team.category().getLongName()).append(") ").
                    append(format(result.duration())).append("\n");
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

            if (raw_result.getBibNumber() == -1)
                times_with_missing_bib_numbers.add(raw_result.getRecordedFinishTime());
        }

        return times_with_missing_bib_numbers;
    }

    private List<Integer> getBibNumbersWithMissingTimes(final Map<Integer, Integer> leg_finished_count) {

        final List<Integer> bib_numbers_with_missing_times = new ArrayList<>();

        for (final RaceEntry entry : ((RelayRace)race).entries) {

            final int number_of_legs_finished = leg_finished_count.getOrDefault(entry.bib_number, 0);

            for (int i = 0; i < ((RelayRace)race).number_of_legs - number_of_legs_finished; i++)
                bib_numbers_with_missing_times.add(entry.bib_number);
        }

        return bib_numbers_with_missing_times;
    }

    private void printDiscrepancies(final OutputStreamWriter writer, final List<Integer> bib_numbers_with_missing_times, final List<Duration> times_with_missing_bib_numbers) throws IOException {

        bib_numbers_with_missing_times.sort(Integer::compareTo);

        writer.append("\nDiscrepancies:\n-------------\n\nBib numbers with missing times: ");

        boolean first = true;
        for (final int bib_number : bib_numbers_with_missing_times) {
            if (!first) writer.append(", ");
            first = false;
            writer.append(String.valueOf(bib_number));
        }

        writer.append("\n\nTimes with missing bib numbers:\n\n");

        for (final Duration time : times_with_missing_bib_numbers)
            writer.append(format(time)).append("\n");
    }

    private void printResult(final OutputStreamWriter writer, final RelayRaceRawResult raw_result, final int legs_already_finished) throws IOException {

        printBibNumberAndTime(writer, raw_result);
        printLegNumber(writer, raw_result, legs_already_finished);
        printComment(writer, raw_result);
    }

    private void printBibNumberAndTime(final OutputStreamWriter writer, final RawResult raw_result) throws IOException {

        final int bib_number = raw_result.getBibNumber();

        writer.append(bib_number != -1 ? String.valueOf(bib_number) : "?").
                append("\t").
                append(raw_result.getRecordedFinishTime() != null ? format(raw_result.getRecordedFinishTime()) : "?");
    }

    private void printLegNumber(final OutputStreamWriter writer, final RelayRaceRawResult raw_result, final int legs_already_finished) throws IOException {

        if (raw_result.getLegNumber() > 0) {

            writer.append("\t").append(String.valueOf(raw_result.getLegNumber()));

            if (legs_already_finished >= raw_result.getLegNumber())
                raw_result.appendComment("Leg "+ raw_result.getLegNumber() + " finisher was runner " + (legs_already_finished + 1) + " to finish for team.");
        }
    }

    private void printComment(final OutputStreamWriter writer, final RelayRaceRawResult raw_result) throws IOException {

        if (!raw_result.getComment().isEmpty()) {

            if (raw_result.getLegNumber() == 0) writer.append("\t");
            writer.append("\t").append(Race.COMMENT_SYMBOL).append(" ").append(raw_result.getComment());
        }

        writer.append("\n");
    }
}

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
import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.single_race.SingleRaceInput;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.grahamkirby.race_timing.common.Normalisation.parseTime;
import static org.grahamkirby.race_timing.relay_race.RelayRace.KEY_ANNOTATIONS_PATH;
import static org.grahamkirby.race_timing.relay_race.RelayRace.KEY_PAPER_RESULTS_PATH;

public class RelayRaceInput extends SingleRaceInput {

    private String paper_results_path, annotations_path;
    private int number_of_raw_results;

    public RelayRaceInput(final Race race) {
        super(race);
    }

    @Override
    protected void readProperties() {

        super.readProperties();

        paper_results_path = race.getProperty(KEY_PAPER_RESULTS_PATH);
        annotations_path = race.getProperty(KEY_ANNOTATIONS_PATH);
    }

    @Override
    protected RaceEntry makeRaceEntry(final List<String> elements) {
        return new RelayRaceEntry(elements, race);
    }

    @Override
    protected void checkForDuplicateEntries(final List<RaceEntry> entries) {

        for (final RaceEntry entry1 : entries) {
            for (final RaceEntry entry2 : entries) {

                final RelayRaceEntry relay_race_entry1 = ((RelayRaceEntry) entry1);
                final RelayRaceEntry relay_race_entry2 = ((RelayRaceEntry) entry2);

                if (relay_race_entry1 != relay_race_entry2 && relay_race_entry1.team.name().equals(relay_race_entry2.team.name()))
                    throw new RuntimeException("duplicate entry: " + relay_race_entry1);
            }
        }
    }

    @Override
    public List<RawResult> loadRawResults() throws IOException {

        final List<RawResult> raw_results = loadRawResults(race.getPath(raw_results_path));
        number_of_raw_results = raw_results.size();

        if (paper_results_path != null)
            raw_results.addAll(loadRawResults(race.getPath(paper_results_path)));

        return raw_results;
    }

    @Override
    protected RawResult loadRawResult(final String line) {

        return new RelayRaceRawResult(line);
    }

    protected int getNumberOfRawResults() {
        return number_of_raw_results;
    }

    protected void loadTimeAnnotations(final List<RawResult> raw_results) throws IOException {

        if (annotations_path != null) {

            final List<String> lines = Files.readAllLines(race.getPath(annotations_path));

            // Skip header line.
            for (int line_index = 1; line_index < lines.size(); line_index++) {

                final String[] elements = lines.get(line_index).split("\t");

                // May add insertion option later.
                if (elements[0].equals("Update"))
                    updateResult(raw_results, elements);
            }
        }
    }

    private static void updateResult(final List<RawResult> raw_results, final String[] elements) {

        final int position = Integer.parseInt(elements[1]);
        final RawResult raw_result = raw_results.get(position - 1);

        if (elements[2].equals("?")) raw_result.setBibNumber(-1);
        else if (!elements[2].isEmpty()) raw_result.setBibNumber(Integer.parseInt(elements[2]));

        if (elements[3].equals("?")) raw_result.setRecordedFinishTime(null);
        else if (!elements[3].isEmpty()) raw_result.setRecordedFinishTime(parseTime(elements[3]));

        if (!elements[4].isEmpty()) raw_result.appendComment(elements[4]);
    }
}

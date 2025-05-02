/*
 * Copyright 2025 Graham Kirby:
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
import java.util.ArrayList;
import java.util.List;

import static org.grahamkirby.race_timing.common.Normalisation.parseTime;
import static org.grahamkirby.race_timing.common.Race.UNKNOWN_BIB_NUMBER;

public class RelayRaceInput extends SingleRaceInput {

    // Configuration file keys.
    private static final String KEY_ANNOTATIONS_PATH = "ANNOTATIONS_PATH";
    private static final String KEY_PAPER_RESULTS_PATH = "PAPER_RESULTS_PATH";

    private String paper_results_path, annotations_path;
    private int number_of_raw_results;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    RelayRaceInput(final Race race) {
        super(race);
    }

    @Override
    protected void readProperties() {

        super.readProperties();

        paper_results_path = race.getOptionalProperty(KEY_PAPER_RESULTS_PATH);
        annotations_path = race.getOptionalProperty(KEY_ANNOTATIONS_PATH);
    }

    @Override
    protected RaceEntry makeRaceEntry(final List<String> elements) {
        return new RelayRaceEntry(elements, race);
    }

    @Override
    public List<RawResult> loadRawResults() throws IOException {

        // Need to copy into a mutable list.
        final List<RawResult> raw_results = new ArrayList<>(loadRawResults(raw_results_path));
        number_of_raw_results = raw_results.size();

        if (paper_results_path != null)
            raw_results.addAll(loadRawResults(paper_results_path));

        return raw_results;
    }

    @Override
    protected RawResult makeRawResult(final String line) {

        try {
            return new RelayRaceRawResult(line);
        } catch (final NumberFormatException _) {
            return null;
        }
    }

    @Override
    public void validateInputFiles() {

        super.validateInputFiles();
        checkResultsContainValidBibNumbers();
    }

    int getNumberOfRawResults() {
        return number_of_raw_results;
    }

    void loadTimeAnnotations(final List<? extends RawResult> raw_results) throws IOException {

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

    private static void updateResult(final List<? extends RawResult> raw_results, final String[] elements) {

        final int position = Integer.parseInt(elements[1]);
        final RawResult raw_result = raw_results.get(position - 1);

        if (elements[2].equals("?")) raw_result.setBibNumber(UNKNOWN_BIB_NUMBER);
        else if (!elements[2].isEmpty()) raw_result.setBibNumber(Integer.parseInt(elements[2]));

        if (elements[3].equals("?")) raw_result.setRecordedFinishTime(null);
        else if (!elements[3].isEmpty()) raw_result.setRecordedFinishTime(parseTime(elements[3]));

        if (!elements[4].isEmpty()) raw_result.appendComment(elements[4]);
    }
}

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
package org.grahamkirby.race_timing.relay_race;


import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.individual_race.TimedRaceInput;
import org.grahamkirby.race_timing.single_race.SingleRace;
import org.grahamkirby.race_timing.single_race.SingleRaceEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.grahamkirby.race_timing_experimental.common.Config.*;
import static org.grahamkirby.race_timing_experimental.common.Normalisation.parseTime;

public class RelayRaceInput extends TimedRaceInput {

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
    protected SingleRaceEntry makeRaceEntry(final List<String> elements) {
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
    protected void validateConfig() {

        super.validateConfig();

        validateMassStartTimes();
        validateDNFRecords();
    }

    @Override
    public void validateInputFiles() {

        super.validateInputFiles();

        validateConfig();
        validateBibNumbersHaveCorrespondingEntry();
        checkNumberOfResults();
    }

    @Override
    protected void validateRequiredPropertiesPresent() {

        super.validateRequiredPropertiesPresent();

        race.getRequiredProperty(KEY_NUMBER_OF_LEGS);
        race.getRequiredProperty(KEY_PAIRED_LEGS);
    }

    @Override
    protected int getNumberOfEntryColumns() {
        return ((RelayRace)race).getNumberOfLegs() + 3;
    }

    private void validateDNFRecords() {

        // TODO update comment and rationalise with TimedIndividualRaceInput.

        // This fills in the DNF results that were specified explicitly in the config
        // file, corresponding to cases where the runners reported not completing the
        // course.

        // Cases where there is no recorded result are captured by the
        // default completion status being DNS.

        final String dnf_string = ((SingleRace) race).dnf_string;

        if (dnf_string != null && !dnf_string.isBlank())
            for (final String individual_dnf_string : dnf_string.split(","))
                try {
                    // String of form "bib-number/leg-number"

                    final String[] elements = individual_dnf_string.split("/");
                    Integer.parseInt(elements[0]);
                    Integer.parseInt(elements[1]);

                } catch (final NumberFormatException _) {
                    throw new RuntimeException(STR."invalid entry '\{dnf_string}' for key '\{KEY_DNF_FINISHERS}' in file '\{race.config_file_path.getFileName()}'");
                }
    }

    private void checkNumberOfResults() {

        try {
            final Map<String, Integer> bib_counts = new HashMap<>();

            countLegResults(bib_counts, raw_results_path);
            countLegResults(bib_counts, paper_results_path);

            for (final Map.Entry<String, Integer> entry : bib_counts.entrySet())
                if (entry.getValue() > ((RelayRace) race).getNumberOfLegs())
                    throw new RuntimeException(STR."surplus result for team '\{entry.getKey()}' in file '\{Path.of(raw_results_path).getFileName()}'");

        } catch (final IOException e) {
            throw new RuntimeException("unexpected IO exception", e);
        }
    }

    private void countLegResults(final Map<String, Integer> bib_counts, final String results_path) throws IOException {

        if (results_path != null)
            for (final String line : Files.readAllLines(race.getPath(results_path)))
                // TODO rationalise with other comment handling. Use stripComment.
                if (!line.startsWith(COMMENT_SYMBOL) && !line.isBlank()) {

                    final String bib_number = line.split("\t")[0];
                    if (!bib_number.equals("?"))
                        bib_counts.put(bib_number, bib_counts.getOrDefault(bib_number, 0) + 1);
                }
    }

    private void validateMassStartTimes() {

        final String mass_start_elapsed_times = race.getOptionalProperty(KEY_MASS_START_ELAPSED_TIMES);

        if (mass_start_elapsed_times != null) {

            Duration previous_time = null;
            for (final String time_string : mass_start_elapsed_times.split(",")) {

                final Duration mass_start_time;
                try {
                    mass_start_time = parseTime(time_string);
                } catch (final DateTimeParseException _) {
                    throw new RuntimeException(STR."invalid mass start time for key '\{KEY_MASS_START_ELAPSED_TIMES}' in file '\{race.config_file_path.getFileName()}'");
                }

                if (previous_time != null && previous_time.compareTo(mass_start_time) > 0)
                    throw new RuntimeException(STR."invalid mass start time order for key '\{KEY_MASS_START_ELAPSED_TIMES}' in file '\{race.config_file_path.getFileName()}'");

                previous_time = mass_start_time;
            }
        }
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

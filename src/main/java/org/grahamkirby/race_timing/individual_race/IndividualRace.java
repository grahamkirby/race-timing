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
package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.common.*;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.common.output.RaceOutputCSV;
import org.grahamkirby.race_timing.common.output.RaceOutputHTML;
import org.grahamkirby.race_timing.common.output.RaceOutputPDF;
import org.grahamkirby.race_timing.common.output.RaceOutputText;
import org.grahamkirby.race_timing.single_race.SingleRace;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("VariableNotUsedInsideIf")
public class IndividualRace extends SingleRace {

    // Configuration file keys.
    private static final String KEY_MEDIAN_TIME = "MEDIAN_TIME";

    private String median_time_string;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public IndividualRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    public static void main(final String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java IndividualRace <config file path>");
        else {
            new IndividualRace(Paths.get(args[0])).processResults();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void calculateResults() {

        // When this race is externally organised as part of a series race, the raw
        // results are not known, and calculation of results is not necessary. In such
        // cases the final results are loaded in configureInputData().
        if (!raw_results.isEmpty()) {

            initialiseResults();

            recordFinishTimes();
            recordDNFs();

            sortResults();
            allocatePrizes();
        }
    }

    /** Gets the entry category for the runner with the given bib number. */
    public EntryCategory findCategory(final int bib_number) {

        return getEntryWithBibNumber(bib_number).runner.category;
    }

    /** Gets the finish time for the given runner. */
    public Duration getRunnerTime(final Runner runner) {

        for (final RaceResult result : getOverallResults()) {

            final IndividualRaceResult individual_result = (IndividualRaceResult) result;
            if (individual_result.entry.runner.equals(runner))
                return individual_result.duration();
        }

        return null;
    }

    /** Gets the median finish time for the race. */
    public Duration getMedianTime() {

        // The median time may be recorded explicitly if not all results are recorded.
        if (median_time_string != null) return Normalisation.parseTime(median_time_string);

        final List<RaceResult> results = getOverallResults();

        if (results.size() % 2 == 0) {

            final IndividualRaceResult median_result1 = (IndividualRaceResult) results.get(results.size() / 2);
            final IndividualRaceResult median_result2 = (IndividualRaceResult) results.get(results.size() / 2 + 1);

            return median_result1.finish_time.plus(median_result2.finish_time).dividedBy(2);

        } else {
            final IndividualRaceResult median_result = (IndividualRaceResult) results.get(results.size() / 2);
            return median_result.finish_time;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Compares the given results on the basis of their finish positions. */
    private int compareRecordedPosition(final RaceResult r1, final RaceResult r2) {

        final int recorded_position1 = getRecordedPosition(((IndividualRaceResult) r1).entry.bib_number);
        final int recorded_position2 = getRecordedPosition(((IndividualRaceResult) r2).entry.bib_number);

        return Integer.compare(recorded_position1, recorded_position2);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void readProperties() throws IOException {

        super.readProperties();
        median_time_string = getProperty(KEY_MEDIAN_TIME);
    }

    @Override
    protected RaceInput getInput() {
        return new IndividualRaceInput(this);
    }

    @Override
    protected RaceOutputCSV getOutputCSV() {
        return new IndividualRaceOutputCSV(this);
    }

    @Override
    protected RaceOutputHTML getOutputHTML() {
        return new IndividualRaceOutputHTML(this);
    }

    @Override
    protected RaceOutputText getOutputText() {
        return new IndividualRaceOutputText(this);
    }

    @Override
    protected RaceOutputPDF getOutputPDF() {
        return new IndividualRaceOutputPDF(this);
    }

    @Override
    protected void outputResults() throws IOException {

        printOverallResults();

        printPrizes();
        printNotes();
        printCombined();
    }

    /** Compares results first by completion status, then by performance, then by recorded position. */
    @Override
    public List<Comparator<RaceResult>> getComparators() {

        return List.of(Race::compareCompletion, Race::comparePerformance, this::compareRecordedPosition);
    }

    /** Compares DNF results first by last name, then by first name. */
    @Override
    public List<Comparator<RaceResult>> getDNFComparators() {

        return List.of(Race::compareRunnerLastName, Race::compareRunnerFirstName);
    }

    @Override
    protected EntryCategory getEntryCategory(final RaceResult result) {

        return ((IndividualRaceResult) result).entry.runner.category;
    }

    @Override
    protected void recordDNF(final String dnf_specification) {

        try {
            final int bib_number = Integer.parseInt(dnf_specification);
            final IndividualRaceResult result = getResultWithBibNumber(bib_number);

            result.completion_status = CompletionStatus.DNF;

        } catch (final NumberFormatException e) {
            throw new RuntimeException("illegal DNF string: ", e);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Initialises overall results from the entries. */
    private void initialiseResults() {

        entries.stream().
            map(entry -> (IndividualRaceEntry) entry).
            map(entry -> new IndividualRaceResult(this, entry)).
            forEachOrdered(overall_results::add);
    }

    private void recordFinishTimes() {

        raw_results.forEach(raw_result -> {

            final IndividualRaceResult result = getResultWithBibNumber(raw_result.getBibNumber());
            result.finish_time = raw_result.getRecordedFinishTime();

            // Provisionally this result is not DNF since a finish time was recorded.
            // However, it might still be set to DNF in recordDNF() if the runner didn't complete the course.
            result.completion_status = CompletionStatus.COMPLETED;
        });
    }

    private IndividualRaceResult getResultWithBibNumber(final int bib_number) {

        return overall_results.stream().
            map(result -> ((IndividualRaceResult) result)).
            filter(result -> result.entry.bib_number == bib_number).
            findFirst().
            orElseThrow(() -> new RuntimeException(STR."unregistered bib number: \{bib_number}"));
    }

    private IndividualRaceEntry getEntryWithBibNumber(final int bib_number) {

        return entries.stream().
            map(entry -> ((IndividualRaceEntry) entry)).
            filter(entry -> entry.bib_number == bib_number).
            findFirst().
            orElseThrow(() -> new RuntimeException(STR."unregistered bib number: \{bib_number}"));
    }

    private int getRecordedPosition(final int bib_number) {

        return (int) raw_results.stream().
            takeWhile(result -> result.getBibNumber() != bib_number).
            count();
    }
}

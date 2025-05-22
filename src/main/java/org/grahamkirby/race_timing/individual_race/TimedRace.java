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

import org.grahamkirby.race_timing.common.CompletionStatus;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.common.output.RaceOutputCSV;
import org.grahamkirby.race_timing.common.output.RaceOutputHTML;
import org.grahamkirby.race_timing.common.output.RaceOutputPDF;
import org.grahamkirby.race_timing.common.output.RaceOutputText;
import org.grahamkirby.race_timing.single_race.SingleRace;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

public abstract class TimedRace extends SingleRace {

//    // Configuration file keys.
//    private static final String KEY_MEDIAN_TIME = "MEDIAN_TIME";
//    private static final String KEY_INDIVIDUAL_EARLY_STARTS = "INDIVIDUAL_EARLY_STARTS";

    public List<TimedRaceEntry> entries;
    protected List<RawResult> raw_results;

//    private String median_time_string;

//    /**
//     * List of individual early starts (usually empty).
//     * Values are read from configuration file using key KEY_INDIVIDUAL_EARLY_STARTS.
//     */
//    private Map<Integer, Duration> early_starts;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public TimedRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    public static void main(final String[] args) throws Exception {

        if (loadProperties(Paths.get(args[0])).containsKey(KEY_RAW_RESULTS_PATH)) {
            commonMain(args, config_file_path -> new TimedIndividualRace(Paths.get(config_file_path)), "TimedIndividualRace");
        } else {
            commonMain(args, config_file_path -> new UntimedIndividualRace(Paths.get(config_file_path)), "UntimedIndividualRace");
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void configureInputData() throws IOException {

        input.validateInputFiles();

        final TimedRaceInput single_race_input = (TimedRaceInput) input;

        entries = single_race_input.loadEntries();

        // Only one of raw_results and overall_results will be fully initialised at this point,
        // depending on whether raw results are available, or just overall results (perhaps for
        // an externally organised race included in a race series).
        // The other list will be initialised as an empty list.
        raw_results = single_race_input.loadRawResults();
//        overall_results = single_race_input.loadOverallResults();
    }

    public List<RawResult> getRawResults() {
        return raw_results;
    }

    @Override
    public void calculateResults() {

        // TODO make subclasses for locally and externally organised.

        // When this race is externally organised as part of a series race, the raw
        // results are not known, and calculation of results is not necessary. In such
        // cases the final results are loaded in configureInputData().
        if (!raw_results.isEmpty()) {

//            initialiseResults();
            recordDNFs();

            sortResults();
            allocatePrizes();
        }
    }

//    @Override
//    protected void configure() throws IOException {
//
//        super.configure();
//        configureIndividualEarlyStarts();
//    }

    /** Gets the entry category for the runner with the given bib number. */
    public EntryCategory findCategory(final int bib_number) {

        return getEntryWithBibNumber(bib_number).participant.category;
    }

//    /** Gets the finish time for the given runner. */
//    public Duration getRunnerTime(final Runner runner) {
//
//        for (final RaceResult result : getOverallResults()) {
//
//            final IndividualRaceResult individual_result = (IndividualRaceResult) result;
//            if (individual_result.entry.runner.equals(runner))
//                return individual_result.duration();
//        }
//
//        return null;
//    }

//    /** Gets the median finish time for the race. */
//    public Duration getMedianTime() {
//
//        // The median time may be recorded explicitly if not all results are recorded.
//        if (median_time_string != null) return parseTime(median_time_string);
//
//        final List<RaceResult> results = getOverallResults();
//
//        if (results.size() % 2 == 0) {
//
//            final IndividualRaceResult median_result1 = (IndividualRaceResult) results.get(results.size() / 2 - 1);
//            final IndividualRaceResult median_result2 = (IndividualRaceResult) results.get(results.size() / 2);
//
//            return median_result1.finish_time.plus(median_result2.finish_time).dividedBy(2);
//
//        } else {
//            final IndividualRaceResult median_result = (IndividualRaceResult) results.get(results.size() / 2);
//            return median_result.finish_time;
//        }
//    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Compares the given results on the basis of their finish positions. */
    private int compareRecordedPosition(final RaceResult r1, final RaceResult r2) {

        final int recorded_position1 = getRecordedPosition(((TimedRaceResult) r1).entry.bib_number);
        final int recorded_position2 = getRecordedPosition(((TimedRaceResult) r2).entry.bib_number);

        return Integer.compare(recorded_position1, recorded_position2);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void readProperties() throws IOException {

        super.readProperties();
//        median_time_string = getOptionalProperty(KEY_MEDIAN_TIME);
    }

//    @Override
//    protected RaceInput getInput() {
//        return new TimedRaceInput(this);
//    }
//
    @Override
    protected RaceOutputCSV getOutputCSV() {
        return new TimedIndividualRaceOutputCSV(this);
    }

    @Override
    protected RaceOutputHTML getOutputHTML() {
        return new TimedIndividualRaceOutputHTML(this);
    }

    @Override
    protected RaceOutputText getOutputText() {
        return new TimedIndividualRaceOutputText(this);
    }

    @Override
    protected RaceOutputPDF getOutputPDF() {
        return new TimedIndividualRaceOutputPDF(this);
    }

    @Override
    protected void outputResults() throws IOException {

        printOverallResults();

        printPrizes();
        printNotes();
        printCombined();
    }

    /** Compares results first by performance with DNFs last, then by recorded position, then by name. */
    @Override
    public List<Comparator<RaceResult>> getComparators() {

        return List.of(ignoreIfBothResultsAreDNF(penaliseDNF(Race::comparePerformance)), ignoreIfEitherResultIsDNF(this::compareRecordedPosition), Race::compareRunnerLastName, Race::compareRunnerFirstName);
    }

    @Override
    protected void recordDNF(final String dnf_specification) {

        final int bib_number = Integer.parseInt(dnf_specification);
        final TimedRaceResult result = getResultWithBibNumber(bib_number);

        result.completion_status = CompletionStatus.DNF;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

//    private void initialiseResults() {
//
//        raw_results.forEach(raw_result -> {
//
//            final int bib_number = raw_result.getBibNumber();
//            final IndividualRaceEntry entry = getEntryWithBibNumber(bib_number);
//
//            // TODO apply in separate operation
//            final Duration early_start_offset = early_starts.getOrDefault(bib_number, Duration.ZERO);
//            final Duration finish_time = raw_result.getRecordedFinishTime().plus(early_start_offset);
//
//            final IndividualRaceResult result = new IndividualRaceResult(this, entry, finish_time);
//
//            overall_results.add(result);
//        });
//    }

    private TimedRaceResult getResultWithBibNumber(final int bib_number) {

        return overall_results.stream().
            map(result -> ((TimedRaceResult) result)).
            filter(result -> result.entry.bib_number == bib_number).
            findFirst().
            orElseThrow();
    }

    TimedIndividualRaceEntry getEntryWithBibNumber(final int bib_number) {

        return entries.stream().
            map(entry -> ((TimedIndividualRaceEntry) entry)).
            filter(entry -> entry.bib_number == bib_number).
            findFirst().
            orElseThrow();
    }

    private int getRecordedPosition(final int bib_number) {

        return (int) raw_results.stream().
            takeWhile(result -> result.getBibNumber() != bib_number).
            count();
    }

//    private void configureIndividualEarlyStarts() {
//
//        final String individual_early_starts_string = getOptionalProperty(KEY_INDIVIDUAL_EARLY_STARTS);
//
//        // bib number / start time difference
//        // Example: INDIVIDUAL_EARLY_STARTS = 2/0:10:00,26/0:20:00
//
//        early_starts = new HashMap<>();
//
//        if (individual_early_starts_string != null)
//            Arrays.stream(individual_early_starts_string.split(",")).
//                forEach(this::recordEarlyStart);
//    }
//
//    private void recordEarlyStart(final String early_starts_string) {
//
//        final String[] split = early_starts_string.split("/");
//
//        final int bib_number = Integer.parseInt(split[0]);
//        final Duration start_time = parseTime(split[1]);
//
//        early_starts.put(bib_number, start_time);
//    }
}

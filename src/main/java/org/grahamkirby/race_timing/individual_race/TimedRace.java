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

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.common.output.RaceOutputCSV;
import org.grahamkirby.race_timing.common.output.RaceOutputHTML;
import org.grahamkirby.race_timing.common.output.RaceOutputPDF;
import org.grahamkirby.race_timing.common.output.RaceOutputText;
import org.grahamkirby.race_timing.single_race.SingleRace;
import org.grahamkirby.race_timing.single_race.SingleRaceResult;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

public abstract class TimedRace extends SingleRace {

    public List<TimedRaceEntry> entries;
    protected List<RawResult> raw_results;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected TimedRace(final Path config_file_path) throws IOException {
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

        final TimedRaceInput race_input = (TimedRaceInput) input;

        entries = race_input.loadEntries();
        raw_results = race_input.loadRawResults();
    }

    public List<RawResult> getRawResults() {
        return raw_results;
    }

    @Override
    public void calculateResults() {

        recordDNFs();
        sortResults();
        allocatePrizes();
    }

    /** Gets the entry category for the runner with the given bib number. */
    public EntryCategory findCategory(final int bib_number) {

        return getEntryWithBibNumber(bib_number).participant.category;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Compares the given results on the basis of their finish positions. */
    private int compareRecordedPosition(final RaceResult r1, final RaceResult r2) {

        final int recorded_position1 = getRecordedPosition(((SingleRaceResult) r1).entry.bib_number);
        final int recorded_position2 = getRecordedPosition(((SingleRaceResult) r2).entry.bib_number);

        return Integer.compare(recorded_position1, recorded_position2);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

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

        return List.of(
            ignoreIfBothResultsAreDNF(penaliseDNF(Race::comparePerformance)),
            ignoreIfEitherResultIsDNF(this::compareRecordedPosition),
            Race::compareRunnerLastName,
            Race::compareRunnerFirstName);
    }

    @Override
    protected void recordDNF(final String dnf_specification) {

        final int bib_number = Integer.parseInt(dnf_specification);
        final TimedRaceResult result = getResultWithBibNumber(bib_number);

        result.dnf = true;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private TimedRaceResult getResultWithBibNumber(final int bib_number) {

        return overall_results.stream().
            map(result -> ((TimedRaceResult) result)).
            filter(result -> result.entry.bib_number == bib_number).
            findFirst().
            orElseThrow();
    }

    protected TimedRaceEntry getEntryWithBibNumber(final int bib_number) {

        return entries.stream().
            map(entry -> ((TimedRaceEntry) entry)).
            filter(entry -> entry.bib_number == bib_number).
            findFirst().
            orElseThrow();
    }

    private int getRecordedPosition(final int bib_number) {

        return (int) raw_results.stream().
            takeWhile(result -> result.getBibNumber() != bib_number).
            count();
    }
}

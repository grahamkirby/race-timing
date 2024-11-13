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
package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.common.RacePrizes;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing.single_race.SingleRace;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class IndividualRace extends SingleRace {

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
    public void processResults() throws IOException {

        calculateResults();
        outputResults();
    }

    @Override
    public void calculateResults() {

        // TODO align structure with SeriesRace
        initialiseResults();

        fillFinishTimes();
        fillDNFs();
        sortResults();
        allocatePrizes();
    }

    @Override
    public boolean allowEqualPositions() {

        // No dead heats for overall results, since an ordering is imposed at finish funnel.
        return false;
    }

    public EntryCategory findCategory(final int bib_number) {
        return getEntryWithBibNumber(bib_number).runner.category;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void configure() throws IOException {

        super.configure();

        configureHelpers();
        configureInputData();
    }

    @Override
    protected void configureHelpers() {

        input = new IndividualRaceInput(this);

        output_CSV = new IndividualRaceOutputCSV(this);
        output_HTML = new IndividualRaceOutputHTML(this);
        output_text = new IndividualRaceOutputText(this);
        output_PDF = new IndividualRaceOutputPDF(this);

        prizes = new RacePrizes(this);
    }

    @Override
    public void initialiseResults() {

        for (int i = 0; i < raw_results.size(); i++)
            overall_results.add(new IndividualRaceResult(this));
    }

    @Override
    protected void outputResults() throws IOException {

        printOverallResults();
        printPrizes();
        printNotes();
        printCombined();
    }

    @Override
    protected void printOverallResults() throws IOException {

        output_CSV.printResults();
        output_HTML.printResults();
    }

    @Override
    protected void printPrizes() throws IOException {

        output_text.printPrizes();
        output_PDF.printPrizes();
        output_HTML.printPrizes();
    }

    @Override
    protected void printNotes() throws IOException {

        output_text.printNotes();
    }

    @Override
    protected void printCombined() throws IOException {

        output_HTML.printCombined();
    }

    @Override
    public List<Comparator<RaceResult>> getComparators() {

        return List.of(IndividualRace::compareRecordedPosition,IndividualRaceResult::comparePerformance, RaceResult::compareCompletion);
    }

    @Override
    public List<Comparator<RaceResult>> getDNFComparators() {

        return List.of(RaceResult::compareRunnerFirstName, RaceResult::compareRunnerLastName);
    }

    @Override
    protected boolean entryCategoryIsEligibleForPrizeCategoryByGender(final EntryCategory entry_category, final PrizeCategory prize_category) {

        return entry_category.getGender().equals(prize_category.getGender());
    }

    @Override
    protected EntryCategory getEntryCategory(RaceResult result) {
        return ((IndividualRaceResult) result).entry.runner.category;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void fillDNF(final String dnf_string) {
        try {
            final String cleaned = dnf_string.strip();

            if (!cleaned.isEmpty()) {

                final int bib_number = Integer.parseInt(cleaned);
                final IndividualRaceResult result = getResultWithBibNumber(bib_number);

                result.DNF = true;
                result.finish_time = DUMMY_DURATION;
            }
        }
        catch (Exception e) {
            throw new RuntimeException("illegal DNF string: " + e.getLocalizedMessage());
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    // TODO rationalise locations of result comparators
    private static int compareRecordedPosition(final RaceResult r1, final RaceResult r2) {

        final IndividualRace individual_race = (IndividualRace) r1.race;

        final int this_recorded_position = individual_race.getRecordedPosition(((IndividualRaceResult)r1).entry.bib_number);
        final int other_recorded_position = individual_race.getRecordedPosition(((IndividualRaceResult)r2).entry.bib_number);

        return Integer.compare(this_recorded_position, other_recorded_position);
    }

    private void fillFinishTimes() {

        for (int results_index = 0; results_index < raw_results.size(); results_index++) {

            final RawResult raw_result = raw_results.get(results_index);
            final IndividualRaceResult result = (IndividualRaceResult)overall_results.get(results_index);

            result.entry = getEntryWithBibNumber(raw_result.getBibNumber());
            result.finish_time = raw_result.getRecordedFinishTime();

            // Provisionally this leg is not DNF since a finish time was recorded.
            // However, it might still be set to DNF in fillDNF() if the runner didn't complete the course.
            result.DNF = false;
        }
    }

    private IndividualRaceResult getResultWithBibNumber(final int bib_number) {

        return overall_results.stream().
                map(result -> ((IndividualRaceResult) result)).
                filter(result -> result.entry.bib_number == bib_number).
                findFirst().
                orElseThrow(() -> new RuntimeException("unrecorded bib number: " + bib_number));
    }

    private IndividualRaceEntry getEntryWithBibNumber(final int bib_number) {

        return entries.stream().
                map(entry -> ((IndividualRaceEntry) entry)).
                filter(entry -> entry.bib_number == bib_number).
                findFirst().
                orElseThrow(() -> new RuntimeException("unregistered bib number: " + bib_number));
    }

    protected int getRecordedPosition(final int bib_number) {

        final AtomicInteger i = new AtomicInteger();

        return raw_results.stream().
            filter(r -> { i.getAndIncrement(); return r.getBibNumber() == bib_number;}).
            map(_ -> i.get()).
            findFirst().
            orElseThrow();
    }
}

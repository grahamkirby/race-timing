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

import org.grahamkirby.race_timing.common.CompletionStatus;
import org.grahamkirby.race_timing.common.RaceInput;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
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
    public void calculateResults() {

        initialiseResults();

        fillFinishTimes();
        fillDNFs();

        sortResults();
        allocatePrizes();
    }

    public EntryCategory findCategory(final int bib_number) {
        return getEntryWithBibNumber(bib_number).runner.category;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private int compareRecordedPosition(final RaceResult r1, final RaceResult r2) {

        final int recorded_position1 = getRecordedPosition(((IndividualRaceResult) r1).entry.bib_number);
        final int recorded_position2 = getRecordedPosition(((IndividualRaceResult) r2).entry.bib_number);

        return Integer.compare(recorded_position1, recorded_position2);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

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
    protected void initialiseResults() {

        entries.stream().
            map(entry -> (IndividualRaceEntry)entry).
            map(entry -> new IndividualRaceResult(this, entry)).
            forEach(overall_results::add);
    }

    @Override
    protected void outputResults() throws IOException {

        printOverallResults();

        printPrizes();
        printNotes();
        printCombined();
    }

    @Override
    public List<Comparator<RaceResult>> getComparators() {

        return List.of(this::compareCompletion, this::comparePerformance, this::compareRecordedPosition);
    }

    @Override
    public List<Comparator<RaceResult>> getDNFComparators() {

        return List.of(this::compareRunnerLastName, this::compareRunnerFirstName);
    }

    @Override
    protected boolean entryCategoryIsEligibleForPrizeCategoryByGender(final EntryCategory entry_category, final PrizeCategory prize_category) {

        return entry_category.getGender().equals(prize_category.getGender());
    }

    @Override
    protected EntryCategory getEntryCategory(RaceResult result) {

        return ((IndividualRaceResult) result).entry.runner.category;
    }

    @Override
    protected void fillDNF(final String dnf_string) {

        try {
            final int bib_number = Integer.parseInt(dnf_string);
            final IndividualRaceResult result = getResultWithBibNumber(bib_number);

            result.completion_status = CompletionStatus.DNF;
        }
        catch (Exception e) {
            throw new RuntimeException("illegal DNF string: ", e);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void fillFinishTimes() {

        raw_results.forEach(raw_result -> {

            final IndividualRaceResult result = getResultWithBibNumber(raw_result.getBibNumber());
            result.finish_time = raw_result.getRecordedFinishTime();

            // Provisionally this result is not DNF since a finish time was recorded.
            // However, it might still be set to DNF in fillDNF() if the runner didn't complete the course.
            result.completion_status = CompletionStatus.COMPLETED;
        });
    }

    private IndividualRaceResult getResultWithBibNumber(final int bib_number) {

        return overall_results.stream().
            map(result -> ((IndividualRaceResult) result)).
            filter(result -> result.entry.bib_number == bib_number).
            findFirst().
            orElseThrow(() -> new RuntimeException("unregistered bib number: " + bib_number));
    }

    private IndividualRaceEntry getEntryWithBibNumber(final int bib_number) {

        return entries.stream().
            map(entry -> ((IndividualRaceEntry) entry)).
            filter(entry -> entry.bib_number == bib_number).
            findFirst().
            orElseThrow(() -> new RuntimeException("unregistered bib number: " + bib_number));
    }

    private int getRecordedPosition(final int bib_number) {

        final AtomicInteger position = new AtomicInteger(0);

        return raw_results.stream().
            peek(_ -> position.incrementAndGet()).
            filter(result -> result.getBibNumber() == bib_number).
            map(_ -> position.get()).
            findFirst().
            orElse(UNKNOWN_RACE_POSITION);
    }
}

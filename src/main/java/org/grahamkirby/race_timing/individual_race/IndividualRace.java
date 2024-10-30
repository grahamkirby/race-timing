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

import org.grahamkirby.race_timing.common.RaceEntry;
import org.grahamkirby.race_timing.common.RacePrizes;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.output.RaceOutputHTML;
import org.grahamkirby.race_timing.single_race.SingleRace;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

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
    public boolean allowEqualPositions() {

        // No dead heats for overall results, since an ordering is imposed at finish funnel.
        return false;
    }

    @Override
    public boolean isEligibleForGender(EntryCategory entry_category, PrizeCategory prize_category) {

        return entry_category.getGender().equals(prize_category.getGender());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void configure() throws IOException {

        super.configure();

        configureHelpers();
        configureInputData();
    }

    @Override
    public void processResults() throws IOException {

        calculateResults();
        outputResults();
    }

    public void calculateResults() {

        initialiseResults();

        fillFinishTimes();
        fillDNFs();
        sortResults();
        allocatePrizes();
    }

    private void outputResults() throws IOException {

        printOverallResults();
        printPrizes();
        printNotes();
        printCombined();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public EntryCategory getEntryCategory(RaceResult result) {
        return ((IndividualRaceResult) result).entry.runner.category;
    }

    private void configureHelpers() {

        input = new IndividualRaceInput(this);

        output_CSV = new IndividualRaceOutputCSV(this);
        output_HTML = new IndividualRaceOutputHTML(this);
        output_text = new IndividualRaceOutputText(this);
        output_PDF = new IndividualRaceOutputPDF(this);

        prizes = new RacePrizes(this);
    }

    private void initialiseResults() {

        for (int i = 0; i < raw_results.size(); i++)
            overall_results.add(new IndividualRaceResult(this));
    }

    private void fillFinishTimes() {

        for (int results_index = 0; results_index < raw_results.size(); results_index++) {

            final RawResult raw_result = raw_results.get(results_index);
            final IndividualRaceResult result = (IndividualRaceResult)overall_results.get(results_index);

            result.entry = findEntryWithBibNumber(raw_result.getBibNumber());
            result.finish_time = raw_result.getRecordedFinishTime();

            // Provisionally this leg is not DNF since a finish time was recorded.
            // However, it might still be set to DNF in fillDNFs() if the runner didn't complete the course.
            result.DNF = false;
        }
    }

    @Override
    protected void fillDNF(final String dnf_string) {
        try {
            final String cleaned = dnf_string.strip();

            if (!cleaned.isEmpty()) {

                final int bib_number = Integer.parseInt(cleaned);
                final IndividualRaceResult result = getResultWithBibNumber(bib_number);
                
                result.DNF = true;
                result.finish_time = Duration.ZERO;
            }
        }
        catch (Exception e) {
            throw new RuntimeException("illegal DNF string: " + e.getLocalizedMessage());
        }
    }

    private IndividualRaceResult getResultWithBibNumber(final int bib_number) {

        for (RaceResult result : overall_results)
            if (((IndividualRaceResult) result).entry.bib_number == bib_number)
                return (IndividualRaceResult) result;

        throw new RuntimeException("unrecorded bib number: " + bib_number);
    }

    protected int getRecordedPosition(final int bib_number) {

        for (int i = 0; i < raw_results.size(); i++) {
            if (raw_results.get(i).getBibNumber() == bib_number) {
                return i + 1;
            }
        }

        return Integer.MAX_VALUE;
    }

    private IndividualRaceEntry findEntryWithBibNumber(final int bib_number) {

        for (RaceEntry entry : entries)
            if (entry.bib_number == bib_number)
                return (IndividualRaceEntry)entry;

        throw new RuntimeException("unregistered bib number: " + bib_number);
    }

    public EntryCategory findCategory(final int bib_number) {

        return findEntryWithBibNumber(bib_number).runner.category;
    }

    private void sortResults() {

        // Sort in order of recorded time.
        // DNF results are sorted in increasing order of bib number.
        // Where two runners have the same recorded time, the order in which they were recorded is preserved.
        overall_results.sort(IndividualRaceResult::compare);
    }

    private void printOverallResults() throws IOException {

        output_CSV.printResults();
        output_HTML.printResults();
    }

    private void printPrizes() throws IOException {

        output_text.printPrizes();
        output_PDF.printPrizes();
        output_HTML.printPrizes();
    }

    private void printNotes() throws IOException {

        output_text.printNotes();
    }

    private void printCombined() throws IOException {

        ((RaceOutputHTML)output_HTML).printCombined();
    }
}

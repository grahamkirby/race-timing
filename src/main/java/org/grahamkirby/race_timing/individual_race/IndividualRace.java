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
import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.common.categories.Category;
import org.grahamkirby.race_timing.common.categories.JuniorRaceCategories;
import org.grahamkirby.race_timing.common.categories.SeniorRaceCategories;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.single_race.SingleRace;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class IndividualRace extends SingleRace {

    ////////////////////////////////////////////  SET UP  ////////////////////////////////////////////
    //                                                                                              //
    //  See README.md at the project root for details of how to configure and run this software.    //
    //                                                                                              //
    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final int DEFAULT_NUMBER_OF_OPEN_PRIZES = 3;
    private static final int DEFAULT_NUMBER_OF_SENIOR_PRIZES = 1;
    private static final int DEFAULT_NUMBER_OF_CATEGORY_PRIZES = 1;

    private boolean senior_race;
    public boolean open_prize_categories, senior_prize_categories;
    public int number_of_open_prizes, number_of_senior_prizes, number_of_category_prizes;

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
    public void configure() throws IOException {

        readProperties();

        configureHelpers();
        configureCategories();
        configureInputData();
    }

    @Override
    public void processResults() throws IOException {

        processResults(true);
    }

    public void processResults(final boolean output_results) throws IOException {

        initialiseResults();

        fillFinishTimes();
        fillDNFs();
        calculateResults();
        allocatePrizes();

        // This is optional so that an individual race can be loaded as part of a series race without generating output.
        if (output_results) {
            printOverallResults();
            printPrizes();
            printNotes();
            printCombined();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void readProperties() {

        senior_race = Boolean.parseBoolean(getPropertyWithDefault(Race.SENIOR_RACE_KEY, "true"));
        open_prize_categories = Boolean.parseBoolean(getPropertyWithDefault(Race.OPEN_PRIZE_CATEGORIES_KEY, "true"));
        senior_prize_categories = Boolean.parseBoolean(getPropertyWithDefault(Race.SENIOR_PRIZE_CATEGORIES_KEY, "false"));
        number_of_open_prizes = Integer.parseInt(getPropertyWithDefault(Race.NUMBER_OF_OPEN_PRIZES_KEY, String.valueOf(DEFAULT_NUMBER_OF_OPEN_PRIZES)));
        number_of_senior_prizes = Integer.parseInt(getPropertyWithDefault(Race.NUMBER_OF_SENIOR_PRIZES_KEY, String.valueOf(DEFAULT_NUMBER_OF_SENIOR_PRIZES)));
        number_of_category_prizes = Integer.parseInt(getPropertyWithDefault(Race.NUMBER_OF_CATEGORY_PRIZES_KEY, String.valueOf(DEFAULT_NUMBER_OF_CATEGORY_PRIZES)));
    }

    private void configureHelpers() {

        input = new IndividualRaceInput(this);

        output_CSV = new IndividualRaceOutputCSV(this);
        output_HTML = new IndividualRaceOutputHTML(this);
        output_text = new IndividualRaceOutputText(this);
        output_PDF = new IndividualRaceOutputPDF(this);

        prizes = new RacePrizes(this);
    }

    private void configureCategories() {

        categories = senior_race ? new SeniorRaceCategories(open_prize_categories, senior_prize_categories, number_of_open_prizes, number_of_senior_prizes, number_of_category_prizes) : new JuniorRaceCategories(number_of_category_prizes);
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
                final IndividualRaceResult result = getResultWithBibNumber(Integer.parseInt(cleaned));
                result.DNF = true;
                result.finish_time = Duration.ZERO;
            }
        }
        catch (Exception e) {
            throw new RuntimeException("illegal DNF time: " + e.getLocalizedMessage());
        }
    }

    public IndividualRaceResult getResultWithBibNumber(final int bib_number) {

        return (IndividualRaceResult)overall_results.get(findResultsIndexOfRunnerWithBibNumber(bib_number));
    }

    int findResultsIndexOfRunnerWithBibNumber(final int bib_number) {

        for (int i = 0; i < overall_results.size(); i++)
            if (((IndividualRaceResult)overall_results.get(i)).entry.bib_number == bib_number)
                return i;

        throw new RuntimeException("unregistered bib number: " + bib_number);
    }

    public int getRecordedPosition(final int bib_number) {

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

    public Category findCategory(int bib_number) {

        return findEntryWithBibNumber(bib_number).runner.category;
    }

    private void calculateResults() {

        // Sort in order of recorded time.
        // DNF results are sorted in increasing order of bib number.
        // Where two runners have the same recorded time, the order in which they were recorded is preserved.
        overall_results.sort(IndividualRaceResult::compare);
    }

    public void printOverallResults() throws IOException {

        output_CSV.printOverallResults(false);
        output_HTML.printOverallResults(true);
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

        output_HTML.printCombined();
    }
}

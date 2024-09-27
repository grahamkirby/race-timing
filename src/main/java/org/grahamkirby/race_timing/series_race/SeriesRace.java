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
package org.grahamkirby.race_timing.series_race;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.individual_race.IndividualRace;
import org.grahamkirby.race_timing.individual_race.IndividualRaceResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class SeriesRace extends Race {

    protected List<IndividualRace> races;
    protected List<Runner> combined_runners;

    protected int number_of_category_prizes;
    protected int minimum_number_of_races;

    public SeriesRace(Path config_file_path) throws IOException {
        super(config_file_path);
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

        initialiseResults();

        calculateResults();
        allocatePrizes();

        printOverallResults();
        printCombined();
        printPrizes();
        printNotes();
    }

    public List<IndividualRace> getRaces() {
        return races;
    }

    public int getMinimumNumberOfRaces() {
        return minimum_number_of_races;
    }

    public int getNumberOfRacesTakenPlace() {

        int number_of_races_completed = 0;

        for (final Race individual_race : getRaces())
            if (individual_race != null) number_of_races_completed++;

        return number_of_races_completed;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void configureInputData() throws IOException {

        races = ((SeriesRaceInput)input).loadRaces();
    }

    protected void readProperties() {

        number_of_category_prizes = Integer.parseInt(getPropertyWithDefault("CATEGORY_PRIZES", String.valueOf(3)));
    }

    private void initialiseResults() {

        combined_runners = new ArrayList<>();

        for (final IndividualRace individual_race : races)
            if (individual_race != null)
                for (final RaceResult result : individual_race.getOverallResults()) {

                    final Runner runner = ((IndividualRaceResult)result).entry.runner;
                    if (!combined_runners.contains(runner))
                        combined_runners.add(runner);
                }
    }

    private void calculateResults() {

        for (final Runner runner : combined_runners)
            overall_results.add(getOverallResult(runner));

        overall_results.sort(getResultsSortComparator());
    }

    private void printOverallResults() throws IOException {

        output_CSV.printOverallResults(false);
        output_HTML.printOverallResults(true);
    }

    private void printNotes() throws IOException {

        output_text.printNotes();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract Comparator<RaceResult> getResultsSortComparator();
    protected abstract RaceResult getOverallResult(final Runner runner);

    protected abstract void configureHelpers();
    protected abstract void configureCategories();

    protected abstract void printPrizes() throws IOException;
    protected abstract void printCombined() throws IOException;
}

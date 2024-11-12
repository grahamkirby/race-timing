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
import java.util.*;

public abstract class SeriesRace extends Race {

    // TODO add support for Grand Prix series.

    protected List<IndividualRace> races;
    protected List<Runner> combined_runners;

    protected int minimum_number_of_races;

    public SeriesRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void configure() throws IOException {

        readProperties();

        super.configure();

        configureHelpers();
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

        return (int) getRaces().stream().filter(Objects::nonNull).count();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void configureInputData() throws IOException {

        races = ((SeriesRaceInput)input).loadRaces();
    }

    protected abstract void readProperties();

    private void initialiseResults() {

        final Set<Runner> combined = new HashSet<>();

        for (final IndividualRace individual_race : races)
            if (individual_race != null)
                for (final RaceResult result : individual_race.getOverallResults())
                    combined.add(((IndividualRaceResult)result).entry.runner);

        combined_runners = new ArrayList<>(combined);
    }

    private void calculateResults() {

        // TODO unify with RelayRace.calculateResults()
        overall_results.addAll(combined_runners.stream().map(this::getOverallResult).toList());

        sortResults();
    }

    private void printOverallResults() throws IOException {

        output_CSV.printResults();
        output_HTML.printResults();
    }

    private void printNotes() throws IOException {

        output_text.printNotes();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract RaceResult getOverallResult(final Runner runner);
    protected abstract void configureHelpers();
    protected abstract void printPrizes() throws IOException;
    protected abstract void printCombined() throws IOException;
}

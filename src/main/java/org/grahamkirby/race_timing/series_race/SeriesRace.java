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
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public abstract class SeriesRace extends Race {

    protected List<IndividualRace> races;

    protected int minimum_number_of_races;

    public SeriesRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void calculateResults() {

        initialiseResults();

        sortResults();
        allocatePrizes();
    }

    @Override
    public boolean allowEqualPositions() {

        // There can be dead heats in overall results, since these are determined by sum of results
        // from multiple races, rather than there being an ordering imposed at a single funnel.
        return true;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void configureInputData() throws IOException {

        races = ((SeriesRaceInput)input).loadRaces();
    }

    @Override
    protected void initialiseResults() {

        final Predicate<RaceResult> inclusion_predicate = getResultInclusionPredicate();

        races.stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getOverallResults().stream()).
            filter(inclusion_predicate).
            map(result -> (IndividualRaceResult) result).
            map(result -> result.entry.runner).
            distinct().
            map(this::getOverallResult).
            forEachOrdered(overall_results::add);
    }

    @Override
    protected void outputResults() throws IOException {

        printOverallResults();
        printPrizes();
        printNotes();
        printCombined();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public List<IndividualRace> getRaces() {
        return races;
    }

    public int getNumberOfRacesInSeries() {
        return races.size();
    }

    public int getMinimumNumberOfRaces() {
        return minimum_number_of_races;
    }

    public int getNumberOfRacesTakenPlace() {

        return (int) getRaces().stream().filter(Objects::nonNull).count();
    }

    public boolean seriesHasCompleted() {
        return getNumberOfRacesTakenPlace() == getNumberOfRacesInSeries();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract RaceResult getOverallResult(final Runner runner);
    protected abstract Predicate<RaceResult> getResultInclusionPredicate();
}

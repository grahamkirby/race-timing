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
package org.grahamkirby.race_timing.series_race;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing.individual_race.IndividualRace;
import org.grahamkirby.race_timing.individual_race.IndividualRaceResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings("IncorrectFormatting")
public abstract class SeriesRace extends Race {

    // Configuration file keys.
    private static final String KEY_NUMBER_OF_RACES_IN_SERIES = "NUMBER_OF_RACES_IN_SERIES";
    private static final String KEY_MINIMUM_NUMBER_OF_RACES = "MINIMUM_NUMBER_OF_RACES";

    protected List<IndividualRace> races;

    private int number_of_races_in_series;
    private int minimum_number_of_races;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected SeriesRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract RaceResult getOverallResult(final Runner runner);
    protected abstract Predicate<RaceResult> getResultInclusionPredicate();
    protected abstract void processMultipleClubsForRunner(String runner_name, List<String> defined_clubs);

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void calculateResults() {

        initialiseResults();

        sortResults();
        allocatePrizes();
    }

    @Override
    public boolean areEqualPositionsAllowed() {

        // There can be dead heats in overall results, since these are determined by sum of results
        // from multiple races, rather than there being an ordering imposed at a single funnel.
        return true;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void configureInputData() throws IOException {

        races = ((SeriesRaceInput) input).loadRaces();
    }

    @Override
    protected void outputResults() throws IOException {

        printOverallResults();
        printPrizes();
        printNotes();
        printCombined();
    }

    @Override
    protected void readProperties() {

        number_of_races_in_series = Integer.parseInt(getProperty(KEY_NUMBER_OF_RACES_IN_SERIES));
        minimum_number_of_races = Integer.parseInt(getProperty(KEY_MINIMUM_NUMBER_OF_RACES));
    }

    @Override
    protected boolean isEntryCategoryEligibleForPrizeCategoryByGender(final EntryCategory entry_category, final PrizeCategory prize_category) {

        return entry_category != null && entry_category.getGender().equals(prize_category.getGender());
    }

    @Override
    protected EntryCategory getEntryCategory(final RaceResult result) {

        return ((SeriesRaceResult) result).runner.category;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public List<IndividualRace> getRaces() {
        return races;
    }

    int getNumberOfRacesInSeries() {
        return number_of_races_in_series;
    }

    public int getMinimumNumberOfRaces() {
        return minimum_number_of_races;
    }

    public int getNumberOfRacesTakenPlace() {

        return (int) races.stream().filter(Objects::nonNull).count();
    }

    protected static int comparePossibleCompletion(final RaceResult r1, final RaceResult r2) {

        return Boolean.compare(((SeriesRaceResult) r2).canCompleteSeries(), ((SeriesRaceResult) r1).canCompleteSeries());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void initialiseResults() {

        final Predicate<RaceResult> inclusion_predicate = getResultInclusionPredicate();

        final List<IndividualRaceResult> results = races.stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getOverallResults().stream()).
            filter(inclusion_predicate).
            map(result -> (IndividualRaceResult) result).toList();

        rationaliseEntryCategories(results);

        results.stream().
            map(result -> result.entry.runner).
            distinct().
            map(this::getOverallResult).
            forEachOrdered(overall_results::add);
    }

    @SuppressWarnings("KeySetIterationMayUseEntrySet")
    private void rationaliseEntryCategories(final Iterable<? extends IndividualRaceResult> results) {

        // 'results' contains relevant results from first race, followed by relevant results from second race, ...

        final Map<Runner, List<IndividualRaceResult>> map = getResultsByRunner(results);

        for (final Runner runner : map.keySet())
            checkCategories(map.get(runner));
    }

    private static Map<Runner, List<IndividualRaceResult>> getResultsByRunner(final Iterable<? extends IndividualRaceResult> results) {

        final Map<Runner, List<IndividualRaceResult>> map = new HashMap<>();

        for (final IndividualRaceResult result : results) {
            final Runner runner = result.entry.runner;
            if (!map.containsKey(runner))
                map.put(runner, new ArrayList<>());
            map.get(runner).add(result);
        }

        return map;
    }

    @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
    private void checkCategories(final List<IndividualRaceResult> runner_results) {

        final EntryCategory earliest_category = getEarliestNonNullCategory(runner_results);

        checkConsistencyOverSeries(runner_results);

        for (final IndividualRaceResult result : runner_results)
            result.entry.runner.category = earliest_category;
    }

    @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
    private void checkConsistencyOverSeries(final Iterable<? extends IndividualRaceResult> runner_results) {

        EntryCategory previous_category = null;

        for (final IndividualRaceResult result : runner_results) {

            final EntryCategory current_category = result.entry.runner.category;

            if (current_category != null) {
                if (previous_category != null && !previous_category.equals(current_category)) {

                    final String note = STR."from \{previous_category.getShortName()} to \{current_category.getShortName()} at \{result.race.getProperty(KEY_RACE_NAME_FOR_RESULTS)}";

                    if (isAgeOrderConsistent(previous_category, current_category))
                        getNotes().append(STR."""
                            Runner \{result.entry.runner.name} changed category \{note}
                            """);
                    else
                        getNotes().append(STR."""
                            Runner \{result.entry.runner.name} illegal category change \{note}
                            """);
                }

                previous_category = current_category;
            }
        }
    }

    private static EntryCategory getEarliestNonNullCategory(final Iterable<? extends IndividualRaceResult> runner_results) {

        EntryCategory first_non_null_category = null;
        for (final IndividualRaceResult result : runner_results) {
            if (result.entry.runner.category != null) {
                first_non_null_category = result.entry.runner.category;
                break;
            }
        }
        return first_non_null_category;
    }

    private static boolean isAgeOrderConsistent(final EntryCategory category1, final EntryCategory category2) {
        return category1.getMinimumAge() <= category2.getMinimumAge();
    }

    protected void printOverallResults() throws IOException {

        super.printOverallResults();

        for (final RaceResult result : overall_results)
            if (((SeriesRaceResult)result).runner.category == null)
                getNotes().append(STR."""
                    Runner \{((SeriesRaceResult) result).runner.name} unknown category so omitted from overall results
                    """);
    }

    protected void configureClubs() {
        getRunnerNames().forEach(this::normaliseClubsForRunner);
    }

    protected void normaliseClubsForRunner(final String runner_name) {

        // Where a runner name is associated with a single entry with a defined club
        // plus some other entries with no club defined, add the club to those entries.

        // Where a runner name is associated with multiple clubs, leave as is, under
        // assumption that they are separate runner_names.
        final List<String> clubs_for_runner = getRunnerClubs(runner_name);
        final List<String> defined_clubs = getDefinedClubs(clubs_for_runner);

        final int number_of_defined_clubs = defined_clubs.size();
        final int number_of_undefined_clubs = clubs_for_runner.size() - number_of_defined_clubs;

        if (number_of_defined_clubs == 1 && number_of_undefined_clubs > 0)
            recordDefinedClubForRunnerName(runner_name, defined_clubs.getFirst());

        if (number_of_defined_clubs > 1) {
            processMultipleClubsForRunner(runner_name, defined_clubs);
        }
    }

    protected void noteMultipleClubsForRunnerName(final String runner_name, final Iterable<String> defined_clubs) {

        getNotes().append(STR."Runner \{runner_name} recorded for multiple clubs: \{String.join(", ", defined_clubs)}\n");
    }

    private static List<String> getDefinedClubs(final Collection<String> clubs) {

        return clubs.stream().filter(SeriesRace::isClubDefined).toList();
    }

    private static boolean isClubDefined(final String club) {
        return !club.equals("?");
    }

    private List<String> getRunnerClubs(final String runner_name) {

        return races.stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getOverallResults().stream()).
            map(result -> (IndividualRaceResult) result).
            map(result -> result.entry.runner).
            filter(runner -> runner.name.equals(runner_name)).
            map(runner -> runner.club).
            distinct().
            sorted().
            toList();
    }

    protected void recordDefinedClubForRunnerName(final String runner_name, final String defined_club) {

        races.stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getOverallResults().stream()).
            map(result -> (IndividualRaceResult) result).
            map(result -> result.entry.runner).
            filter(runner -> runner.name.equals(runner_name)).
            forEachOrdered(runner -> runner.club = defined_club);
    }

    private List<String> getRunnerNames() {

        return races.stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getOverallResults().stream()).
            map(result -> (IndividualRaceResult) result).
            map(result -> result.entry.runner.name).
            distinct().
            toList();
    }
}

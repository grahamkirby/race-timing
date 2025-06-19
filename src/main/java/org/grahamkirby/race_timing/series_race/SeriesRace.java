/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (graham.kirby@st-andrews.ac.uk)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.series_race;


import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.single_race.SingleRace;
import org.grahamkirby.race_timing.single_race.SingleRaceResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings("IncorrectFormatting")
public abstract class SeriesRace extends Race {

    // Configuration file keys.
    protected static final String KEY_NUMBER_OF_RACES_IN_SERIES = "NUMBER_OF_RACES_IN_SERIES";
    protected static final String KEY_MINIMUM_NUMBER_OF_RACES = "MINIMUM_NUMBER_OF_RACES";

    protected List<SingleRace> races;

    private int number_of_races_in_series;
    private int minimum_number_of_races;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected SeriesRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract RaceResult getOverallResult(Runner runner);
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

        // There can be dead heats in overall results, since these are determined by combination of results
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

        number_of_races_in_series = Integer.parseInt(getRequiredProperty(KEY_NUMBER_OF_RACES_IN_SERIES));
        minimum_number_of_races = Integer.parseInt(getRequiredProperty(KEY_MINIMUM_NUMBER_OF_RACES));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public List<SingleRace> getRaces() {
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

    /**
     * The order of the individual races in 'races' is set by the order in which they are listed in the config file,
     * and this defines the order in which the races will be displayed in the results table.
     *
     * This method defines the temporal order in which races take place. In this default implementation both orders
     * are the same, but it may be overridden to allow non-temporal order results listing in some types of series race.
     *
     * @param position a temporal position in the series, with zero corresponding to the first race to take place
     * @return the number of the race taking place in that temporal position, as defined in the ordering in the configuration
     */
    protected int getRaceNumberInTemporalPosition(final int position) {
        return position;
    }

    protected static int comparePossibleCompletion(final RaceResult r1, final RaceResult r2) {

        return Boolean.compare(r2.canComplete(), r1.canComplete());
    }

    protected static int compareNumberOfRacesCompleted(final RaceResult r1, final RaceResult r2) {

        final int minimum_races_to_qualify = ((SeriesRace) r1.race).minimum_number_of_races;

        final int relevant_number_of_races_r1 = Math.min(((SeriesRaceResult) r1).numberOfRacesCompleted(), minimum_races_to_qualify);
        final int relevant_number_of_races_r2 = Math.min(((SeriesRaceResult) r2).numberOfRacesCompleted(), minimum_races_to_qualify);

        return -Integer.compare(relevant_number_of_races_r1, relevant_number_of_races_r2);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void initialiseResults() {

        getResultsByRunner().forEach(this::checkCategoryConsistencyOverSeries);

        overall_results = new ArrayList<>(
            getRacesInTemporalOrder().stream().
                filter(Objects::nonNull).
                flatMap(race -> race.getOverallResults().stream()).
                filter(getResultInclusionPredicate()).
                map(result -> (Runner)((SingleRaceResult) result).entry.participant).
                distinct().
                map(this::getOverallResult).
                toList());
    }

    private List<List<SingleRaceResult>> getResultsByRunner() {

        final Map<Runner, List<SingleRaceResult>> map = new HashMap<>();

        getRacesInTemporalOrder().stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getOverallResults().stream()).
            filter(getResultInclusionPredicate()).
            map(result -> ((SingleRaceResult) result)).
            forEachOrdered(result -> {
                final Runner runner = (Runner)result.entry.participant;
                map.putIfAbsent(runner, new ArrayList<>());
                map.get(runner).add(result);
            });

        return new ArrayList<>(map.values());
    }

    private List<SingleRace> getRacesInTemporalOrder() {

        final List<SingleRace> races_in_order = new ArrayList<>();

        for (int i = 0; i < number_of_races_in_series; i++)
            races_in_order.add(races.get(getRaceNumberInTemporalPosition(i)));

        return races_in_order;
    }

    @SuppressWarnings({"NonBooleanMethodNameMayNotStartWithQuestion", "BoundedWildcard"})
    private void checkCategoryConsistencyOverSeries(final List<SingleRaceResult> runner_results) {

        EntryCategory earliest_category = null;
        EntryCategory previous_category = null;
        EntryCategory last_category = null;

        for (final SingleRaceResult result : runner_results) {

            final EntryCategory current_category = result.entry.participant.category;

            if (current_category != null) {

                if (earliest_category == null)
                    earliest_category = current_category;

                last_category = current_category;

                if (previous_category != null && !previous_category.equals(current_category)) {

                    final String race_name = result.race.getRequiredProperty(KEY_RACE_NAME_FOR_RESULTS);

                    checkForChangeToYoungerAgeCategory(result, previous_category, current_category, race_name);
                    checkForChangeToDifferentGenderCategory(result, previous_category, current_category, race_name);

                    getNotes().append(STR."""
                        Runner \{result.entry.participant.name} changed category from \{previous_category.getShortName()} to \{current_category.getShortName()} at \{race_name}
                        """);
                }

                previous_category = current_category;
            }
        }

        checkForChangeToTooMuchOlderAgeCategory(runner_results.getFirst(), earliest_category, last_category);

        for (final SingleRaceResult result : runner_results)
            result.entry.participant.category = earliest_category;
    }

    @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
    private static void checkForChangeToYoungerAgeCategory(final SingleRaceResult result, final EntryCategory previous_category, final EntryCategory current_category, final String race_name) {

        if (previous_category != null && current_category != null && current_category.getMinimumAge() < previous_category.getMinimumAge())
            throw new RuntimeException(STR."invalid category change: runner '\{result.entry.participant.name}' changed from \{previous_category.getShortName()} to \{current_category.getShortName()} at \{race_name}");
    }

    @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
    private static void checkForChangeToDifferentGenderCategory(final SingleRaceResult result, final EntryCategory previous_category, final EntryCategory current_category, final String race_name) {

        if (previous_category != null && current_category != null && !current_category.getGender().equals(previous_category.getGender()))
            throw new RuntimeException(STR."invalid category change: runner '\{result.entry.participant.name}' changed from \{previous_category.getShortName()} to \{current_category.getShortName()} at \{race_name}");
    }

    @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
    private static void checkForChangeToTooMuchOlderAgeCategory(final SingleRaceResult result, final EntryCategory earliest_category, final EntryCategory last_category) {

        if (earliest_category != null && last_category != null && last_category.getMinimumAge() > earliest_category.getMaximumAge() + 1)
            throw new RuntimeException(STR."invalid category change: runner '\{result.entry.participant.name}' changed from \{earliest_category.getShortName()} to \{last_category.getShortName()} during series");
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

    private void normaliseClubsForRunner(final String runner_name) {

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

        if (number_of_defined_clubs > 1)
            processMultipleClubsForRunner(runner_name, defined_clubs);
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
            map(result -> (SingleRaceResult) result).
            map(result -> result.entry.participant).
            filter(participant -> participant.name.equals(runner_name)).
            map(participant -> ((Runner)participant).club).
            distinct().
            sorted().
            toList();
    }

    protected void recordDefinedClubForRunnerName(final String runner_name, final String defined_club) {

        races.stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getOverallResults().stream()).
            map(result -> (SingleRaceResult) result).
            map(result -> result.entry.participant).
            filter(participant -> participant.name.equals(runner_name)).
            forEachOrdered(participant -> ((Runner)participant).club = defined_club);
    }

    private List<String> getRunnerNames() {

        return races.stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getOverallResults().stream()).
            map(result -> (SingleRaceResult) result).
            map(result -> result.entry.participant.name).
            distinct().
            toList();
    }
}

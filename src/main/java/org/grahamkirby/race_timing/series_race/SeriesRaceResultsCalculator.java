/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (race-timing@kirby-family.net)
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

import org.grahamkirby.race_timing.categories.EntryCategory;
import org.grahamkirby.race_timing.common.*;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;

import static org.grahamkirby.race_timing.common.Config.KEY_RACE_NAME_FOR_RESULTS;
import static org.grahamkirby.race_timing.common.Config.LINE_SEPARATOR;

public abstract class SeriesRaceResultsCalculator extends RaceResultsCalculator {

    abstract RaceResult getOverallResult(final Runner runner);

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public SeriesRaceResultsCalculator(final RaceInternal race) {
        super(race);
    }

    public void calculateResults() {

        checkCategoryConsistencyOverSeries();
        initialiseResults();
        sortResults();
        allocatePrizes();
    }

    @Override
    public boolean areEqualPositionsAllowed() {

        return true;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    Predicate<RaceResult> getResultInclusionPredicate() {

        return (_ -> true);
    }

    int getRaceNumberInTemporalPosition(final int position) {
        return position;
    }

    static Duration getRunnerTime(final SingleRaceInternal individual_race, final Runner runner) {

        for (final RaceResult result : individual_race.getResultsCalculator().getOverallResults()) {

            final SingleRaceResult individual_result = (SingleRaceResult) result;
            if (individual_result.getParticipant().equals(runner))
                return individual_result.duration();
        }

        return null;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void initialiseResults() {

        overall_results = new ArrayList<>(
            getRacesInTemporalOrder().stream().
                filter(Objects::nonNull).
                flatMap(race -> race.getResultsCalculator().getOverallResults().stream()).
                filter(getResultInclusionPredicate()).
                map(result -> (Runner) result.getParticipant()).
                distinct().
                map(this::getOverallResult).
                toList());
    }

    private List<List<SingleRaceResult>> getResultsByRunner() {

        final Map<Runner, List<SingleRaceResult>> map = new HashMap<>();

        getRacesInTemporalOrder().stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getResultsCalculator().getOverallResults().stream()).
            filter(getResultInclusionPredicate()).
            map(result -> ((SingleRaceResult) result)).
            forEachOrdered(result -> {
                final Runner runner = (Runner) result.getParticipant();
                map.putIfAbsent(runner, new ArrayList<>());
                map.get(runner).add(result);
            });

        return new ArrayList<>(map.values());
    }

    private List<SingleRaceInternal> getRacesInTemporalOrder() {

        final List<SingleRaceInternal> races = ((SeriesRace) race).getRaces();
        final List<SingleRaceInternal> races_in_order = new ArrayList<>();

        // TODO write as permutation.
        for (int i = 0; i < races.size(); i++)
            races_in_order.add(races.get(getRaceNumberInTemporalPosition(i)));

        return races_in_order;
    }

    private void checkCategoryConsistencyOverSeries() {

        getResultsByRunner().forEach(this::checkCategoryConsistencyOverSeries);
    }

    private void checkCategoryConsistencyOverSeries(final List<SingleRaceResult> runner_results) {

        EntryCategory earliest_category = null;
        EntryCategory previous_category = null;
        EntryCategory last_category = null;

        for (final SingleRaceResult result : runner_results) {

            final EntryCategory current_category = result.getParticipant().getCategory();

            if (current_category != null) {

                if (earliest_category == null)
                    earliest_category = current_category;

                last_category = current_category;

                if (previous_category != null && !previous_category.equals(current_category)) {

                    final String race_name = (String) result.getRace().getConfig().get(KEY_RACE_NAME_FOR_RESULTS);

                    checkForChangeToYoungerAgeCategory(result, previous_category, current_category, race_name);
                    checkForChangeToDifferentGenderCategory(result, previous_category, current_category, race_name);

                    race.getNotes().appendToNotes("Runner " + result.getParticipantName() + " changed category from " + previous_category.getShortName() + " to " + current_category.getShortName() + " at " + race_name + LINE_SEPARATOR);
                }

                previous_category = current_category;
            }
        }

        checkForChangeToTooMuchOlderAgeCategory(runner_results.getFirst(), earliest_category, last_category);

        for (final SingleRaceResult result : runner_results)
            result.getParticipant().setCategory(earliest_category);
    }

    private static void checkForChangeToYoungerAgeCategory(final SingleRaceResult result, final EntryCategory previous_category, final EntryCategory current_category, final String race_name) {

        if (previous_category != null && current_category != null && current_category.getMinimumAge() < previous_category.getMinimumAge())
            throw new RuntimeException("invalid category change: runner '" + result.getParticipantName() + "' changed from " + previous_category.getShortName() + " to " + current_category.getShortName() + " at " + race_name);
    }

    private static void checkForChangeToDifferentGenderCategory(final SingleRaceResult result, final EntryCategory previous_category, final EntryCategory current_category, final String race_name) {

        if (previous_category != null && current_category != null && !current_category.getGender().equals(previous_category.getGender()))
            throw new RuntimeException("invalid category change: runner '" + result.getParticipantName() + "' changed from " + previous_category.getShortName() + " to " + current_category.getShortName() + " at " + race_name);
    }

    private static void checkForChangeToTooMuchOlderAgeCategory(final SingleRaceResult result, final EntryCategory earliest_category, final EntryCategory last_category) {

        if (earliest_category != null && last_category != null && last_category.getMinimumAge() > earliest_category.getMaximumAge() + 1)
            throw new RuntimeException("invalid category change: runner '" + result.getParticipantName() + "' changed from " + earliest_category.getShortName() + " to " + last_category.getShortName() + " during series");
    }
}

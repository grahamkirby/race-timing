/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2026 Graham Kirby (race-timing@kirby-family.net)
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
package org.grahamkirby.race_timing.common;

import org.grahamkirby.race_timing.categories.CategoriesProcessor;
import org.grahamkirby.race_timing.categories.PrizeCategory;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.grahamkirby.race_timing.common.Config.*;

public abstract class RaceResultsProcessor implements RaceResults {

    //////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Calculations generic to all race types. Features/assumptions:
    //
    //     * Bib numbers are unique within a given race.
    //     * Times are recorded with 1 second resolution.
    //     * Runners that finish may still be separately recorded as DNF (did not finish) if reported.
    //     * Dead heats can be optionally accommodated.
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected RaceInternal race;
    protected List<RaceResult> overall_results;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public abstract void calculateResults() throws IOException;
    public abstract boolean canDistinguishFromOtherEqualPerformances(RaceResult result);

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public RaceResultsProcessor(final RaceInternal race) {
        this.race = race;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public List<RaceResult> getOverallResults() {
        return overall_results;
    }

    @Override
    public List<RaceResult> getOverallResults(final List<PrizeCategory> prize_categories) {

        final Predicate<RaceResult> prize_category_filter = result ->
            race.getCategoriesProcessor().isResultEligibleInSomePrizeCategory(result.getEntryCategory(), getClub(result), prize_categories);

        final List<RaceResult> results = makeMutableCopy(overall_results.stream().filter(prize_category_filter).toList());

        setPositionStrings(results);
        return results;
    }

    @Override
    public List<RaceResult> getPrizeWinners(final PrizeCategory prize_category) {

        final List<RaceResult> prize_results = makeMutableCopy(overall_results.stream().
            filter(result -> result.getCategoriesOfPrizesAwarded().contains(prize_category)).
            toList());

        setPositionStrings(prize_results);
        return prize_results;
    }

    @Override
    public List<String> getPrizeCategoryGroups() {
        return race.getCategoriesProcessor().getPrizeCategoryGroups();
    }

    @Override
    public List<PrizeCategory> getPrizeCategoriesByGroup(final String group) {
        return race.getCategoriesProcessor().getPrizeCategoriesByGroup(group);
    }

    @Override
    public boolean arePrizesInThisOrLaterCategory(final PrizeCategory category) {

        for (final PrizeCategory other_category : race.getCategoriesProcessor().getPrizeCategories().reversed()) {

            if (!getPrizeWinners(other_category).isEmpty()) return true;
            if (category.equals(other_category) && !arePrizesInOtherCategoryWithSameMinimumAge(category)) return false;
        }
        throw new RuntimeException();
    }

    @Override
    public Config getConfig() {
        return race.getConfig();
    }

    @Override
    public NormalisationProcessor getNormalisationProcessor() {
        return race.getNormalisationProcessor();
    }

    @Override
    public NotesProcessor getNotesProcessor() {
        return race.getNotesProcessor();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Sets the position string for each result. These are recorded as strings rather than ints so
     *  that equal results can be recorded as e.g. "13=". Whether or not equal positions are allowed
     *  is determined by the second parameter. */
    public void setPositionStrings(final List<? extends RaceResult> results) {

        setPositionStrings(results, this::canDistinguishFromOtherEqualPerformances);
    }

    private static class BoxedResult <T extends RaceResult> {
        T value;
    }

    /** Sets the position string for each result. These are recorded as strings rather than ints so
     *  that equal results can be recorded as e.g. "13=". Whether or not equal positions are allowed
     *  is determined by the second parameter. */
    public <T extends RaceResult> void setPositionStrings(final List<T> results, final Function<RaceResult, Boolean> can_distinguish_equal_performances) {

        // Sets position strings for dead heats, if allowed by the can_distinguish_equal_performances predicate.
        // E.g. if results 3 and 4 have the same time, both will be set to "3=".

        // The predicate is passed in rather than using canDistinguishEqualPerformances() since that applies to the race overall.
        // In a series race the individual races don't allow equal positions, but the race overall does.
        // Conversely in a relay race the legs after the first leg do allow equal positions.

        for (int result_index = 0; result_index < results.size(); result_index++) {

            final T result = results.get(result_index);

            if (result.canOrHasCompleted()) {

                // Skip over any following results with the same performance.
                // Defined in terms of performance rather than duration, since in some races ranking is determined
                // by scores rather than times.

                final int length_of_sequence_of_equal_performances = getLengthOfSequenceOfEqualPerformances(results, result_index);

                final boolean no_results_in_sequence_allow_equal_positions = results.stream().
                    skip(result_index).
                    limit(length_of_sequence_of_equal_performances).
                    allMatch(can_distinguish_equal_performances::apply);

                if (!no_results_in_sequence_allow_equal_positions) {

                    // Sequence is non-empty, and there are results following this one that should have equal positions.
                    recordEqualPositions(results, result_index, length_of_sequence_of_equal_performances);

                    // Index will also be incremented in main loop, so subtract one here.
                    result_index += length_of_sequence_of_equal_performances - 1;
                } else
                    // Sequence is empty, or no results in sequence allow equal positions, so just record current position for this one.
                    result.setPositionString(String.valueOf(result_index + 1));

            } else
                result.setPositionString("-");
        }

        // Sort results again, since setting equal positions may have altered the sort order,
        // e.g. equal positions may be listed in alphabetical order of runner or team name.
        results.sort(null);
    }

    private static <T extends RaceResult> int getLengthOfSequenceOfEqualPerformances(final List<T> results, final int result_index) {

        final BoxedResult<T> previous_result = new BoxedResult<>();
        previous_result.value = results.get(result_index);

        final int count = (int) results.stream().
            skip(result_index + 1).
            takeWhile(result -> {
                final boolean equal_performance_to_previous_result = result.comparePerformanceTo(previous_result.value) == 0;
                previous_result.value = result;
                return equal_performance_to_previous_result;
            }).
            count();

        // Can't have sequence of length 1.
        return count == 0 ? 0 : count + 1;
    }
    
    /** Records the same position for the given range of results. */
    private static void recordEqualPositions(final List<? extends RaceResult> results, final int start_index, final int length_of_sequence_of_equal_performances) {

        final String position_string = (start_index + 1) + "=";

        results.stream().
            skip(start_index).
            limit(length_of_sequence_of_equal_performances).
            forEach(result -> result.setPositionString(position_string));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Sorts all results by relevant comparators. */
    protected void sortOverallResults() {

        overall_results.sort(null);
    }

    protected void allocatePrizes() {

        final CategoriesProcessor categories_processor = race.getCategoriesProcessor();
        final List<PrizeCategory> categories = categories_processor.getPrizeCategoriesInDecreasingGeneralityOrder();

        final boolean prefer_lower_prize = (boolean) race.getConfig().get(KEY_PREFER_LOWER_PRIZE_IN_MORE_GENERAL_CATEGORY);

        if (prefer_lower_prize) {

            // Allocate all prizes in each category, in decreasing order of category generality.
            // This means e.g. a V40 runner in second place overall will win second prize in open
            // category rather than first prize in V40 category.
            allocatePrizes(categories, number_of_prizes -> number_of_prizes);
        }
        else {
            // Allocate first prize in each category first, in decreasing order of category generality.
            // This means e.g. a 40+ relay team in second place overall will win first in 40+ category
            // rather than second prize in open category.

            // Allocate first prizes in each category.
            allocatePrizes(categories, _ -> 1);

            // Allocate remaining prizes in each category.
            allocatePrizes(categories, number_of_prizes -> number_of_prizes - 1);
        }
    }

    protected void recordDNFs() {

        // This fills in the DNF results that were specified explicitly in the config
        // file, corresponding to cases where the runners reported not completing the
        // course.

        // Cases where there is no recorded result are captured by the
        // default completion status being DNS.

        // Comma-separated sequence of bib-numbers for any runners that have a time
        // recorded but they DNF'd. e.g.
        // DNF_FINISHERS = 6,11
        final String dnf_string = (String) race.getConfig().get(KEY_DNF_FINISHERS);

        if (dnf_string != null && !dnf_string.isBlank())
            for (final String individual_dnf_string : dnf_string.split(","))
                recordDNF(individual_dnf_string);
    }

    protected abstract void recordDNF(final String dnf_specification);

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void allocatePrizes(final List<PrizeCategory> prize_categories, final Function<Integer, Integer> get_number_of_prizes_to_allocate) {

        for (final PrizeCategory category : prize_categories) {

            // May allocate more prizes than this if there are further places tied with a prize winner.
            final int prizes_to_allocate = get_number_of_prizes_to_allocate.apply(category.numberOfPrizes());

            int prizes_allocated = 0;
            boolean previous_was_dead_heat = true;
            Performance previous_performance = null;

            for (final RaceResult result : overall_results)
                if (isEligibleInCategory(result, category))
                    if (prizes_allocated < prizes_to_allocate || result.getPerformance().equals(previous_performance) && previous_was_dead_heat) {

                        result.getCategoriesOfPrizesAwarded().add(category);
                        prizes_allocated++;
                        previous_was_dead_heat = !canDistinguishFromOtherEqualPerformances(result);
                        previous_performance = result.getPerformance();
                    }
        }
    }

    private boolean isEligibleInCategory(final RaceResult result, final PrizeCategory prize_category) {

        if (!result.canOrHasCompleted()) return false;
        if (prize_category.isExclusive() && result.getCategoriesOfPrizesAwarded().stream().anyMatch(PrizeCategory::isExclusive)) return false;

        return race.getCategoriesProcessor().isResultEligibleForPrizeCategory(result.getParticipant().category, getClub(result), prize_category);
    }

    private String getClub(final RaceResult result) {

        return result.getParticipant() instanceof final Runner runner ? runner.getClub() : null;
    }

    private boolean arePrizesInOtherCategoryWithSameMinimumAge(final PrizeCategory category) {

        return race.getCategoriesProcessor().getPrizeCategories().stream().
            filter(other_category -> other_category.getAgeRange().getMinimumAge() == category.getAgeRange().getMinimumAge()).
            anyMatch(other_category -> !getPrizeWinners(other_category).isEmpty());
    }
}

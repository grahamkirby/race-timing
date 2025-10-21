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
package org.grahamkirby.race_timing.common;

import org.grahamkirby.race_timing.categories.PrizeCategory;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.grahamkirby.race_timing.common.Config.KEY_DNF_FINISHERS;

public abstract class RaceResultsCalculator {

    protected Race2 race;
    protected List<RaceResult> overall_results;
    protected final StringBuilder notes;

    protected RaceResultsCalculator() {

        notes = new StringBuilder();
    }

    public abstract void calculateResults();

    public void setRace(final Race2 race) {

        this.race = race;
    }

    public StringBuilder getNotes() {
        return notes;
    }

    public List<RaceResult> getOverallResults() {
        return overall_results;
    }

    public boolean arePrizesInThisOrLaterCategory(final PrizeCategory category) {

        for (final PrizeCategory category2 : race.getCategoryDetails().getPrizeCategories().reversed()) {

            if (!getPrizeWinners(category2).isEmpty()) return true;
            if (category.equals(category2) && !arePrizesInOtherCategorySameAge(category)) return false;
        }
        return false;
    }

    /** Gets all the results eligible for the given prize categories. */
    public List<RaceResult> getOverallResults(final List<PrizeCategory> prize_categories) {

        final Predicate<RaceResult> prize_category_filter = result -> race.getCategoryDetails().isResultEligibleInSomePrizeCategory(getClub(result), race.getNormalisation().gender_eligibility_map, result.getCategory(), prize_categories);
        final List<RaceResult> results = overall_results.stream().filter(prize_category_filter).toList();

        setPositionStrings(results, areEqualPositionsAllowed());
        return results;
    }

    /** Returns prize winners in given category. */
    public List<RaceResult> getPrizeWinners(final PrizeCategory prize_category) {

        final List<RaceResult> prize_results = overall_results.stream().
            filter(result -> result.getCategoriesOfPrizesAwarded().contains(prize_category)).
            toList();

        setPositionStrings(prize_results, areEqualPositionsAllowed());

        return prize_results;
    }

    /** Sets the position string for each result. These are recorded as strings rather than ints so
     *  that equal results can be recorded as e.g. "13=". Whether or not equal positions are allowed
     *  is determined by the second parameter. */
    public static void setPositionStrings(final List<? extends RaceResult> results, final boolean allow_equal_positions) {

        // Sets position strings for dead heats, if allowed by the allow_equal_positions flag.
        // E.g. if results 3 and 4 have the same time, both will be set to "3=".

        // The flag is passed in rather than using race.allowEqualPositions() since that applies to the race overall.
        // In a series race the individual races don't allow equal positions, but the race overall does.
        // Conversely in a relay race the legs after the first leg do allow equal positions.

        for (int result_index = 0; result_index < results.size(); result_index++) {

            final RaceResult result = results.get(result_index);

            if (result.canComplete()) {
                if (allow_equal_positions) {

                    // Skip over any following results with the same performance.
                    // Defined in terms of performance rather than duration, since in some races ranking is determined
                    // by scores rather than times.
                    final int highest_index_with_same_performance = getHighestIndexWithSamePerformance(results, result_index);

                    if (highest_index_with_same_performance > result_index) {

                        // There are results following this one with the same performance.
                        recordEqualPositions(results, result_index, highest_index_with_same_performance);
                        result_index = highest_index_with_same_performance;
                    } else
                        // The following result has a different performance, so just record current position for this one.
                        result.setPositionString(String.valueOf(result_index + 1));
                } else {
                    result.setPositionString(String.valueOf(result_index + 1));
                }
            } else {
                result.setPositionString("-");
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract boolean areEqualPositionsAllowed();

    /** Sorts all results by relevant comparators. */
    protected void sortResults() {

        overall_results.sort(null);
    }

    protected void allocatePrizes() {

        for (final PrizeCategory category : race.getCategoryDetails().getPrizeCategories())
            setPrizeWinners(category);
    }

    protected boolean isPrizeWinner(final RaceResult result, final PrizeCategory prize_category) {

        return result.canComplete() &&
            isStillEligibleForPrize(result, prize_category) &&
            race.getCategoryDetails().isResultEligibleForPrizeCategory(getClub(result), race.getNormalisation().gender_eligibility_map, result.getParticipant().category, prize_category);
    }

    protected static void setPrizeWinner(final RaceResult result, final PrizeCategory category) {

        result.getCategoriesOfPrizesAwarded().add(category);
    }

    protected String getClub(final RaceResult result) {

        return result.getParticipant() instanceof final Runner runner ? runner.getClub() : null;
    }

    protected void recordDNFs() {

        // This fills in the DNF results that were specified explicitly in the config
        // file, corresponding to cases where the runners reported not completing the
        // course.

        // Cases where there is no recorded result are captured by the
        // default completion status being DNS.

        final String dnf_string = (String) race.getConfig().get(KEY_DNF_FINISHERS);

        if (dnf_string != null && !dnf_string.isBlank())
            for (final String individual_dnf_string : dnf_string.split(","))
                recordDNF(individual_dnf_string);
    }

    protected void recordDNF(final String dnf_specification) {
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

     /** Records the same position for the given range of results. */
    private static void recordEqualPositions(final List<? extends RaceResult> results, final int start_index, final int end_index) {

        final String position_string = (start_index + 1) + "=";

        for (int i = start_index; i <= end_index; i++)
            results.get(i).setPositionString(position_string);
    }

    /** Finds the highest index for which the performance is the same as the given index. */
    private static int getHighestIndexWithSamePerformance(final List<? extends RaceResult> results, final int start_index) {

        int highest_index_with_same_result = start_index;

        while (highest_index_with_same_result < results.size() - 1 &&
            results.get(highest_index_with_same_result).comparePerformanceTo(results.get(highest_index_with_same_result + 1)) == 0)

            highest_index_with_same_result++;

        return highest_index_with_same_result;
    }

    private static boolean isStillEligibleForPrize(final RaceResult result, final PrizeCategory new_prize_category) {

        if (!new_prize_category.isExclusive()) return true;

        for (final PrizeCategory category_already_won : result.getCategoriesOfPrizesAwarded())
            if (category_already_won.isExclusive()) return false;

        return true;
    }

    private void setPrizeWinners(final PrizeCategory category) {

        final AtomicInteger position = new AtomicInteger(1);

        overall_results.stream().
            filter(_ -> position.get() <= category.numberOfPrizes()).
            filter(result -> isPrizeWinner(result, category)).
            forEachOrdered(result -> {
                position.getAndIncrement();
                setPrizeWinner(result, category);
            });
    }

    private boolean arePrizesInOtherCategorySameAge(final PrizeCategory category) {

        return race.getCategoryDetails().getPrizeCategories().stream().
            filter(cat -> !cat.equals(category)).
            filter(cat -> cat.getMinimumAge() == category.getMinimumAge()).
            anyMatch(cat -> !getPrizeWinners(cat).isEmpty());
    }
}

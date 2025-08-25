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
package org.grahamkirby.race_timing_experimental.series_race;

import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing_experimental.common.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.grahamkirby.race_timing_experimental.common.Config.KEY_RACE_NAME_FOR_RESULTS;

public class MidweekRaceResultsCalculatorImpl implements RaceResultsCalculator {

    private Race race;

    private List<RaceResult> overall_results;
    private final StringBuilder notes;

    MidweekRaceResultsCalculatorImpl() {

        notes = new StringBuilder();
    }

    @Override
    public void setRace(Race race) {

        this.race = race;
    }

    @Override
    public void calculateResults() {

        initialiseResults();
        sortResults();
        allocatePrizes();
    }

    @Override
    public StringBuilder getNotes() {
        return notes;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void initialiseResults() {

        getResultsByRunner().forEach(this::checkCategoryConsistencyOverSeries);

        overall_results = new ArrayList<>(
            getRacesInTemporalOrder().stream().
                filter(Objects::nonNull).
                flatMap(race -> race.getResultsCalculator().getOverallResults().stream()).
                filter(getResultInclusionPredicate()).
                map(result -> (Runner)((SingleRaceResult) result).entry.participant).
                distinct().
                map(this::getOverallResult).
                toList());
    }

    protected RaceResult getOverallResult(final Runner runner) {

        final List<Integer> scores = ((MidweekRaceImpl)race.getSpecific()).getRaces().stream().
            map(individual_race -> ((MidweekRaceImpl) race.getSpecific()).calculateRaceScore(individual_race, runner)).
            toList();

        return new MidweekRaceResult(runner, scores, race);
    }

    protected Predicate<RaceResult> getResultInclusionPredicate() {

        return (_ -> true);
    }

    private List<List<SingleRaceResult>> getResultsByRunner() {

        final Map<Runner, List<SingleRaceResult>> map = new HashMap<>();

        getRacesInTemporalOrder().stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getResultsCalculator().getOverallResults().stream()).
            filter(getResultInclusionPredicate()).
            map(result -> ((SingleRaceResult) result)).
            forEachOrdered(result -> {
                final Runner runner = (Runner)result.entry.participant;
                map.putIfAbsent(runner, new ArrayList<>());
                map.get(runner).add(result);
            });

        return new ArrayList<>(map.values());
    }

    private List<Race> getRacesInTemporalOrder() {

        final List<Race> races = ((MidweekRaceImpl) race.getSpecific()).getRaces();
        final List<Race> races_in_order = new ArrayList<>();

        for (int i = 0; i < races.size(); i++)
            races_in_order.add(races.get(getRaceNumberInTemporalPosition(i)));

        return races_in_order;
    }

    private int getRaceNumberInTemporalPosition(final int position) {
        return position;
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

                    final String race_name = (String) result.race.getConfig().get(KEY_RACE_NAME_FOR_RESULTS);

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

    private void allocatePrizes() {

        for (final PrizeCategory category : race.getCategoryDetails().getPrizeCategories())
            setPrizeWinners(category);
    }

    /** Returns prize winners in given category. */
    public List<RaceResult> getPrizeWinners(final PrizeCategory prize_category) {

        final List<RaceResult> prize_results = overall_results.stream().
            filter(result -> result.categories_of_prizes_awarded.contains(prize_category)).
            toList();

        setPositionStrings(prize_results);

        return prize_results;
    }

    private void setPrizeWinners(PrizeCategory category) {

        final AtomicInteger position = new AtomicInteger(1);

        overall_results.stream().
            filter(_ -> position.get() <= category.numberOfPrizes()).
            filter(result -> isPrizeWinner(result, category)).
            forEachOrdered(result -> {
                position.getAndIncrement();
                setPrizeWinner(result, category);
            });
    }

    protected boolean isPrizeWinner(final RaceResult result, final PrizeCategory prize_category) {

        return result.canComplete() &&
            isStillEligibleForPrize(result, prize_category) &&
            race.getCategoryDetails().isResultEligibleForPrizeCategory(((MidweekRaceResult)result).runner.club, race.getNormalisation().gender_eligibility_map, result.getCategory(), prize_category);
    }

    private static boolean isStillEligibleForPrize(final RaceResult result, final PrizeCategory new_prize_category) {

        if (!new_prize_category.isExclusive()) return true;

        for (final PrizeCategory category_already_won : result.categories_of_prizes_awarded)
            if (category_already_won.isExclusive()) return false;

        return true;
    }

    protected static void setPrizeWinner(final RaceResult result, final PrizeCategory category) {

        result.categories_of_prizes_awarded.add(category);
    }

    /** Sorts all results by relevant comparators. */
    protected void sortResults() {

        overall_results.sort(combineComparators(getComparators()));
    }

    protected List<Comparator<RaceResult>> getComparators() {

        return List.of(penaliseDNF(MidweekRaceResultsCalculatorImpl::comparePerformance), MidweekRaceResultsCalculatorImpl::compareRunnerLastName, MidweekRaceResultsCalculatorImpl::compareRunnerFirstName);
    }

    /** Compares two results based on their performances, which may be based on a single or aggregate time,
     *  or a score. Gives a negative result if the first result has a better performance than the second. */
    public static int comparePerformance(final RaceResult r1, final RaceResult r2) {

        return r1.comparePerformanceTo(r2);
    }

    /** Compares two results based on alphabetical ordering of the runners' first names. */
    public static int compareRunnerFirstName(final RaceResult r1, final RaceResult r2) {

        return Normalisation.getFirstNameOfFirstRunner(r1.getParticipant().name).compareTo(Normalisation.getFirstNameOfFirstRunner(r2.getParticipant().name));
    }

    /** Compares two results based on alphabetical ordering of the runners' last names. */
    public static int compareRunnerLastName(final RaceResult r1, final RaceResult r2) {

        return Normalisation.getLastNameOfFirstRunner(r1.getParticipant().name).compareTo(Normalisation.getLastNameOfFirstRunner(r2.getParticipant().name));
    }

    protected static Comparator<RaceResult> penaliseDNF(final Comparator<? super RaceResult> base_comparator) {

        return (r1, r2) -> {

            if (!r1.canComplete() && r2.canComplete()) return 1;
            if (r1.canComplete() && !r2.canComplete()) return -1;

            return base_comparator.compare(r1, r2);
        };
    }

    protected static Comparator<RaceResult> ignoreIfEitherResultIsDNF(final Comparator<? super RaceResult> base_comparator) {

        return (r1, r2) -> {

            if (!r1.canComplete() || !r2.canComplete()) return 0;
            else return base_comparator.compare(r1, r2);
        };
    }

    protected static Comparator<RaceResult> ignoreIfBothResultsAreDNF(final Comparator<? super RaceResult> base_comparator) {

        return (r1, r2) -> {

            if (!r1.canComplete() && !r2.canComplete()) return 0;
            else return base_comparator.compare(r1, r2);
        };
    }

    /** Combines multiple comparators into a single comparator. */
    protected static Comparator<RaceResult> combineComparators(final Collection<Comparator<RaceResult>> comparators) {

        return comparators.stream().
            reduce((_, _) -> 0, Comparator::thenComparing);
    }

    /** Sets the position string for each result. These are recorded as strings rather than ints so
     *  that equal results can be recorded as e.g. "13=". Whether or not equal positions are allowed
     *  is determined by the particular race type. */
    void setPositionStrings(final List<RaceResult> results) {

        setPositionStrings(results, true);
    }

    /** Sets the position string for each result. These are recorded as strings rather than ints so
     *  that equal results can be recorded as e.g. "13=". Whether or not equal positions are allowed
     *  is determined by the second parameter. */
    protected static void setPositionStrings(final List<RaceResult> results, final boolean allow_equal_positions) {

        // Sets position strings for dead heats, if allowed by the allow_equal_positions flag.
        // E.g. if results 3 and 4 have the same time, both will be set to "3=".

        // The flag is passed in rather than using race.allowEqualPositions() since that applies to the race overall.
        // In a series race the individual races don't allow equal positions, but the race overall does.
        // Conversely in a relay race the legs after the first leg do allow equal positions.

        for (int result_index = 0; result_index < results.size(); result_index++) {

            final RaceResult result = results.get(result_index);

            if (result.shouldDisplayPosition()) {
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
                        result.position_string = String.valueOf(result_index + 1);
                } else {
                    result.position_string = String.valueOf(result_index + 1);
                }
            } else {
                result.position_string = "-";
            }
        }
    }

    /** Records the same position for the given range of results. */
    private static void recordEqualPositions(final List<RaceResult> results, final int start_index, final int end_index) {

        final String position_string = STR."\{start_index + 1}=";

        for (int i = start_index; i <= end_index; i++)
            results.get(i).position_string = position_string;
    }

    /** Finds the highest index for which the performance is the same as the given index. */
    private static int getHighestIndexWithSamePerformance(final List<RaceResult> results, final int start_index) {

        int highest_index_with_same_result = start_index;

        while (highest_index_with_same_result < results.size() - 1 &&
            results.get(highest_index_with_same_result).comparePerformanceTo(results.get(highest_index_with_same_result + 1)) == 0)

            highest_index_with_same_result++;

        return highest_index_with_same_result;
    }

    public List<RaceResult> getOverallResults() {
        return overall_results;
    }

    /** Gets all the results eligible for the given prize categories. */
    public List<RaceResult> getOverallResults(final List<PrizeCategory> prize_categories) {

        final Predicate<RaceResult> prize_category_filter = r -> {
            MidweekRaceResult result = (MidweekRaceResult) r;
            return race.getCategoryDetails().isResultEligibleInSomePrizeCategory(result.runner.club, race.getNormalisation().gender_eligibility_map, result.getCategory(), prize_categories);
        };

        final List<RaceResult> results = overall_results.stream().filter(prize_category_filter).toList();
        setPositionStrings(results);
        return results;
    }

    public boolean arePrizesInThisOrLaterCategory(final PrizeCategory category) {

        for (final PrizeCategory category2 : race.getCategoryDetails().getPrizeCategories().reversed()) {

            if (!getPrizeWinners(category2).isEmpty()) return true;
            if (category.equals(category2) && !arePrizesInOtherCategorySameAge(category)) return false;
        }
        return false;
    }

    private boolean arePrizesInOtherCategorySameAge(final PrizeCategory category) {

        return race.getCategoryDetails().getPrizeCategories().stream().
            filter(cat -> !cat.equals(category)).
            filter(cat -> cat.getMinimumAge() == category.getMinimumAge()).
            anyMatch(cat -> !getPrizeWinners(cat).isEmpty());
    }
}

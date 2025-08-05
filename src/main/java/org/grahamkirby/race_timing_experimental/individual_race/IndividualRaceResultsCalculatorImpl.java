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
package org.grahamkirby.race_timing_experimental.individual_race;

import org.grahamkirby.race_timing.common.Normalisation;
import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing_experimental.common.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.grahamkirby.race_timing.common.Normalisation.parseTime;
import static org.grahamkirby.race_timing_experimental.common.Config.KEY_DNF_FINISHERS;
import static org.grahamkirby.race_timing_experimental.common.Config.KEY_INDIVIDUAL_EARLY_STARTS;

public class IndividualRaceResultsCalculatorImpl implements RaceResultsCalculator {

    private Race race;

    private List<RaceResult> overall_results;
    private StringBuilder notes;

    @Override
    public void setRace(Race race) {
        this.race = race;
        notes = new StringBuilder();
    }

    @Override
    public void calculateResults() {

        initialiseResults();
        configureIndividualEarlyStarts();
        recordDNFs();
        sortResults();
        allocatePrizes();
    }

    @Override
    public StringBuilder getNotes() {
        return notes;
    }

    private void configureIndividualEarlyStarts() {

        final String individual_early_starts_string = (String) race.getConfig().get(KEY_INDIVIDUAL_EARLY_STARTS);

        // bib number / start time difference
        // Example: INDIVIDUAL_EARLY_STARTS = 2/0:10:00,26/0:20:00

        if (individual_early_starts_string != null)
            Arrays.stream(individual_early_starts_string.split(",")).
                forEach(this::recordEarlyStart);
    }

    private void recordEarlyStart(final String early_starts_string) {

        final String[] split = early_starts_string.split("/");

        final int bib_number = Integer.parseInt(split[0]);
        final Duration offset = parseTime(split[1]);

        final SingleRaceResult result = (SingleRaceResult) getResultWithBibNumber(bib_number);

        result.finish_time = result.finish_time.plus(offset);
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
            race.getCategoryDetails().isResultEligibleForPrizeCategory(((Runner)((SingleRaceResult)result).entry.participant).club, race.getNormalisation().gender_eligibility_map, ((SingleRaceResult)result).entry.participant.category, prize_category);
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

    private void initialiseResults() {

        List<RawResult> raw_results = race.getRaceData().getRawResults();

        overall_results = raw_results.stream().
            map(this::makeResult).
            toList();

        overall_results = new ArrayList<>(overall_results);
    }

    private RaceResult makeResult(final RawResult raw_result) {

        final int bib_number = raw_result.getBibNumber();
        final Duration finish_time = raw_result.getRecordedFinishTime();

        return new SingleRaceResult(race, getEntryWithBibNumber(bib_number), finish_time);
    }

    protected void recordDNFs() {

        // This fills in the DNF results that were specified explicitly in the config
        // file, corresponding to cases where the runners reported not completing the
        // course.

        // Cases where there is no recorded result are captured by the
        // default completion status being DNS.

        String dnf_string = (String) race.getConfig().get(KEY_DNF_FINISHERS);

        if (dnf_string != null && !dnf_string.isBlank())
            for (final String individual_dnf_string : dnf_string.split(","))
                recordDNF(individual_dnf_string);
    }

    protected void recordDNF(final String dnf_specification) {

        final int bib_number = Integer.parseInt(dnf_specification);
        final SingleRaceResult result = (SingleRaceResult) getResultWithBibNumber(bib_number);

        result.dnf = true;
    }

    /** Sorts all results by relevant comparators. */
    protected void sortResults() {

        overall_results.sort(combineComparators(getComparators()));
    }

    public List<Comparator<RaceResult>> getComparators() {

        return List.of(
            ignoreIfBothResultsAreDNF(penaliseDNF(IndividualRaceResultsCalculatorImpl::comparePerformance)),
            ignoreIfEitherResultIsDNF(this::compareRecordedPosition),
            IndividualRaceResultsCalculatorImpl::compareRunnerLastName,
            IndividualRaceResultsCalculatorImpl::compareRunnerFirstName);
    }

    /** Compares the given results on the basis of their finish positions. */
    private int compareRecordedPosition(final RaceResult r1, final RaceResult r2) {

        final int recorded_position1 = getRecordedPosition(((SingleRaceResult)r1).entry.bib_number);
        final int recorded_position2 = getRecordedPosition(((SingleRaceResult)r2).entry.bib_number);

        return Integer.compare(recorded_position1, recorded_position2);
    }

    private int getRecordedPosition(final int bib_number) {

        List<RawResult> raw_results = race.getRaceData().getRawResults();

        return (int) raw_results.stream().
            takeWhile(result -> result.getBibNumber() != bib_number).
            count();
    }

    /** Compares two results based on their performances, which may be based on a single or aggregate time,
     *  or a score. Gives a negative result if the first result has a better performance than the second. */
    public static int comparePerformance(final RaceResult r1, final RaceResult r2) {

        return r1.comparePerformanceTo(r2);
    }

    /** Compares two results based on alphabetical ordering of the runners' first names. */
    public static int compareRunnerFirstName(final RaceResult r1, final RaceResult r2) {

        return Normalisation.getFirstName(r1.getParticipant().name).compareTo(Normalisation.getFirstName(r2.getParticipant().name));
    }

    /** Compares two results based on alphabetical ordering of the runners' last names. */
    public static int compareRunnerLastName(final RaceResult r1, final RaceResult r2) {

        return Normalisation.getLastName(r1.getParticipant().name).compareTo(Normalisation.getLastName(r2.getParticipant().name));
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

    protected RaceEntry getEntryWithBibNumber(final int bib_number) {

        List<RaceEntry> entries = race.getRaceData().getEntries();

        return entries.stream().
            filter(entry -> entry.bib_number == bib_number).
            findFirst().
            orElseThrow();
    }

    private RaceResult getResultWithBibNumber(final int bib_number) {

        return overall_results.stream().
            map(result -> (SingleRaceResult) result).
            filter(result -> result.entry.bib_number == bib_number).
            findFirst().
            orElseThrow();
    }

    public boolean areEqualPositionsAllowed() {

        // No dead heats for overall results, since an ordering is imposed at the finish.
        return false;
    }

    /** Sets the position string for each result. These are recorded as strings rather than ints so
     *  that equal results can be recorded as e.g. "13=". Whether or not equal positions are allowed
     *  is determined by the particular race type. */
    void setPositionStrings(final List<RaceResult> results) {

        setPositionStrings(results, areEqualPositionsAllowed());
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
            SingleRaceResult result = (SingleRaceResult) r;
            return race.getCategoryDetails().isResultEligibleInSomePrizeCategory(((Runner)result.entry.participant).club, race.getNormalisation().gender_eligibility_map, result.entry.participant.category, prize_categories);
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

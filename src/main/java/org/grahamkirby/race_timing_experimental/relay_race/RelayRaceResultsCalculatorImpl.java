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
package org.grahamkirby.race_timing_experimental.relay_race;

import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing_experimental.common.*;

import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;

import static org.grahamkirby.race_timing.common.Race.UNKNOWN_BIB_NUMBER;
import static org.grahamkirby.race_timing_experimental.common.Config.KEY_DNF_FINISHERS;

public class RelayRaceResultsCalculatorImpl implements RaceResultsCalculator {

    private static final int UNKNOWN_LEG_NUMBER = 0;
    private static final List<String> GENDER_ORDER = Arrays.asList("Open", "Women", "Mixed");

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private Race race;
    private RelayRaceImpl race_impl;

    private List<RaceResult> overall_results;
    private final StringBuilder notes;

    /** Provides functionality for inferring missing bib number or timing data in the results. */
    private RelayRaceMissingData missing_data;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public RelayRaceResultsCalculatorImpl() {
        notes = new StringBuilder();
    }

    public void setRace(Race race) {

        this.race = race;
        race_impl = ((RelayRaceImpl) race.getSpecific());
        missing_data = new RelayRaceMissingData(race);
    }

    @Override
    public void calculateResults() {

        initialiseResults();
        interpolateMissingTimes();
        guessMissingBibNumbers();

        recordFinishTimes();
        recordStartTimes();
        recordDNFs();

        sortResults();
        allocatePrizes();

        addPaperRecordingComments();
    }

    @Override
    public List<RaceResult> getOverallResults() {
        return overall_results;
    }

    @Override
    public StringBuilder getNotes() {
        return notes;
    }

    /** Returns prize winners in given category. */
    @Override
    public List<RaceResult> getPrizeWinners(final PrizeCategory prize_category) {

        final List<RaceResult> prize_results = overall_results.stream().
            filter(result -> result.categories_of_prizes_awarded.contains(prize_category)).
            toList();

        setPositionStrings(prize_results);

        return prize_results;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void interpolateMissingTimes() {

        missing_data.interpolateMissingTimes();
    }

    private void guessMissingBibNumbers() {

        missing_data.guessMissingBibNumbers();
    }

    private void recordFinishTimes() {

        recordLegResults();
        sortLegResults();
    }

    private void recordLegResults() {

        race.getRaceData().getRawResults().stream().
            filter(result -> result.getBibNumber() != UNKNOWN_BIB_NUMBER).
            forEachOrdered(this::recordLegResult);
    }

    private void recordLegResult(final RawResult raw_result) {

        final int team_index = findIndexOfTeamWithBibNumber(raw_result.getBibNumber());
        final RelayRaceResult result = (RelayRaceResult) overall_results.get(team_index);

        final int leg_index = findIndexOfNextUnfilledLegResult(result.leg_results);
        final LegResult leg_result = result.leg_results.get(leg_index);

        leg_result.finish_time = raw_result.getRecordedFinishTime().plus(race_impl.getStartOffset());

        // Leg number will be zero in most cases, unless explicitly recorded in raw results.
        leg_result.leg_number = ((RelayRaceDataImpl) race.getRaceData()).explicitly_recorded_leg_numbers.getOrDefault(raw_result, UNKNOWN_LEG_NUMBER);

        // Provisionally this leg is not DNF since a finish time was recorded.
        // However, it might still be set to DNF in fillDNFs() if the runner missed a checkpoint.
        leg_result.dnf = false;
    }

    void sortLegResults() {

        overall_results.forEach(RelayRaceResultsCalculatorImpl::sortLegResults);
    }

    private static void sortLegResults(final RaceResult result) {

        final List<LegResult> leg_results = ((RelayRaceResult) result).leg_results;

        // Sort by explicitly recorded leg number.
        leg_results.sort(Comparator.comparingInt(o -> o.leg_number));

        // Reset the leg numbers according to new positions in leg sequence.
        for (int leg_index = 1; leg_index <= leg_results.size(); leg_index++)
            leg_results.get(leg_index - 1).leg_number = leg_index;
    }

    private LegResult getLegResult(final int bib_number, final int leg_number) {

        final RelayRaceResult result = (RelayRaceResult) overall_results.get(findIndexOfTeamWithBibNumber(bib_number));

        return result.leg_results.get(leg_number - 1);
    }

    private void recordStartTimes() {

        overall_results.forEach(this::fillLegResultDetails);
    }

    private void fillLegResultDetails(final RaceResult result) {

        for (int leg_index = 0; leg_index < race_impl.getNumberOfLegs(); leg_index++)
            fillLegResultDetails(((RelayRaceResult) result).leg_results, leg_index);
    }

    private void fillLegResultDetails(final List<? extends LegResult> leg_results, final int leg_index) {

        final LegResult leg_result = leg_results.get(leg_index);

        final Duration individual_start_time = getIndividualStartTime(leg_result, leg_index);
        final Duration leg_mass_start_time = race_impl.getStartTimesForMassStarts().get(leg_index);
        final Duration previous_team_member_finish_time = leg_index > 0 ? leg_results.get(leg_index - 1).finish_time : null;

        leg_result.start_time = getLegStartTime(individual_start_time, leg_mass_start_time, previous_team_member_finish_time, leg_index);

        // Record whether the runner started in a mass start.
        leg_result.in_mass_start = isInMassStart(individual_start_time, leg_mass_start_time, previous_team_member_finish_time, leg_index);
    }

    private Duration getIndividualStartTime(final LegResult leg_result, final int leg_index) {

        return race_impl.getIndividualStarts().stream().
            filter(individual_leg_start -> individual_leg_start.bib_number() == leg_result.entry.bib_number).
            filter(individual_leg_start -> individual_leg_start.leg_number() == leg_index + 1).
            map(RelayRaceImpl.IndividualStart::start_time).
            findFirst().
            orElse(null);
    }

    private static Duration getLegStartTime(final Duration individual_start_time, final Duration mass_start_time, final Duration previous_team_member_finish_time, final int leg_index) {

        // Individual leg time recorded for this runner.
        if (individual_start_time != null) return individual_start_time;

        // Leg 1 runner_names start at time zero if there's no individual time recorded.
        if (leg_index == 0) return Duration.ZERO;

        // No finish time recorded for previous runner, so we can't record a start time for this one.
        // This leg result will be set to DNF by default.
        if (previous_team_member_finish_time == null) return null;

        // Use the earlier of the mass start time and the previous runner's finish time.
        return !mass_start_time.equals(Duration.ZERO) && mass_start_time.compareTo(previous_team_member_finish_time) < 0 ? mass_start_time : previous_team_member_finish_time;
    }

    @SuppressWarnings("TypeMayBeWeakened")
    private boolean isInMassStart(final Duration individual_start_time, final Duration mass_start_time, final Duration previous_runner_finish_time, final int leg_index) {

        // Not in mass start if there is an individually recorded time, or it's the first leg.
        if (individual_start_time != null || leg_index == 0) return false;

        // No previously recorded leg time, so record this runner as starting in mass start if it's a mass start leg.
        if (previous_runner_finish_time == null) return race_impl.getMassStartLegs().get(leg_index);

        // Record this runner as starting in mass start if the previous runner finished after the relevant mass start.
        return !mass_start_time.equals(Duration.ZERO) && mass_start_time.compareTo(previous_runner_finish_time) < 0;
    }

    @SuppressWarnings({"TypeMayBeWeakened", "IfCanBeAssertion"})
    private static int findIndexOfNextUnfilledLegResult(final List<? extends LegResult> leg_results) {

        return (int) leg_results.stream().
            takeWhile(result -> result.finish_time != null).
            count();
    }

    @SuppressWarnings({"IfCanBeAssertion"})
    private int findIndexOfTeamWithBibNumber(final int bib_number) {

        return (int) overall_results.stream().
            map(result -> (RelayRaceResult)result).
            takeWhile(result -> result.entry.bib_number != bib_number).
            count();
    }

    private void addPaperRecordingComments() {

        final List<RawResult> raw_results = race.getRaceData().getRawResults();
        final int number_of_electronically_recorded_results = ((RelayRaceDataImpl) race.getRaceData()).number_of_electronically_recorded_raw_results;

        if (number_of_electronically_recorded_results < raw_results.size())
            raw_results.get(number_of_electronically_recorded_results - 1).appendComment("Remaining times from paper recording sheet only.");
    }

    private void allocatePrizes() {

        // Allocate first prize in each category first, in decreasing order of category breadth.
        // This is because e.g. a 40+ team should win first in 40+ category before a subsidiary
        // prize in open category.

        final List<PrizeCategory> categories_sorted_by_decreasing_generality = sortByDecreasingGenerality(race.getCategoryDetails().getPrizeCategories());

        allocateFirstPrizes(categories_sorted_by_decreasing_generality);
        allocateMinorPrizes(categories_sorted_by_decreasing_generality);
    }

    private static List<PrizeCategory> sortByDecreasingGenerality(final List<PrizeCategory> prize_categories) {

        final List<PrizeCategory> sorted_categories = new ArrayList<>(prize_categories);

        sorted_categories.sort(Comparator.comparingInt((PrizeCategory category) -> category.getMinimumAge()).thenComparingInt(category -> GENDER_ORDER.indexOf(category.getGender())));

        return sorted_categories;
    }

    private void allocateFirstPrizes(final Iterable<PrizeCategory> prize_categories) {

        for (final PrizeCategory category : prize_categories)
            for (final RaceResult result : getOverallResults())
                if (isPrizeWinner(result, category)) {
                    setPrizeWinner(result, category);
                    break;
                }
    }

    private void allocateMinorPrizes(final Iterable<PrizeCategory> prize_categories) {

        for (final PrizeCategory category : prize_categories)
            allocateMinorPrizes(category);
    }

    private void allocateMinorPrizes(final PrizeCategory category) {

        int position = 2;

        for (final RaceResult result : getOverallResults()) {

            if (position > category.numberOfPrizes()) return;

            if (isPrizeWinner(result, category)) {
                setPrizeWinner(result, category);
                position++;
            }
        }
    }

    private boolean isPrizeWinner(final RaceResult result, final PrizeCategory prize_category) {

        return result.canComplete() &&
            isStillEligibleForPrize(result, prize_category) &&
            race.getCategoryDetails().isResultEligibleForPrizeCategory(null, race.getNormalisation().gender_eligibility_map, ((RelayRaceResult) result).entry.participant.category, prize_category);
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

        final Collection<Integer> bib_numbers_seen = new HashSet<>();

        overall_results = race.getRaceData().getRawResults().stream().
            filter(raw_result -> raw_result.getBibNumber() != 0).
            filter(raw_result -> bib_numbers_seen.add(raw_result.getBibNumber())).
            map(this::makeResult).
            toList();

        overall_results = makeMutable(overall_results);
    }

    public static List<RaceResult> makeMutable(final List<? extends RaceResult> results) {
        return new ArrayList<>(results);
    }

    private RaceResult makeResult(final RawResult raw_result) {

        final RaceEntry entry = getEntryWithBibNumber(raw_result.getBibNumber());
        return new RelayRaceResult(race, entry, null);
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

        try {
            // String of form "bib-number/leg-number"

            final String[] elements = dnf_specification.split("/");
            final int bib_number = Integer.parseInt(elements[0]);
            final int leg_number = Integer.parseInt(elements[1]);

            getLegResult(bib_number, leg_number).dnf = true;

        } catch (final NumberFormatException e) {
            throw new RuntimeException(dnf_specification, e);
        }
    }

    /** Sorts all results by relevant comparators. */
    protected void sortResults() {

        overall_results.sort(combineComparators(getComparators()));
    }

    public List<Comparator<RaceResult>> getComparators() {

        return List.of(
            ignoreIfBothResultsAreDNF(penaliseDNF(RelayRaceResultsCalculatorImpl::comparePerformance)),
            RelayRaceResultsCalculatorImpl::compareTeamName);
    }

    /** Compares two results based on their performances, which may be based on a single or aggregate time,
     *  or a score. Gives a negative result if the first result has a better performance than the second. */
    protected static int comparePerformance(final RaceResult r1, final RaceResult r2) {

        return r1.comparePerformanceTo(r2);
    }

    /** Compares two results based on alphabetical ordering of the team name. */
    protected static int compareTeamName(final RaceResult r1, final RaceResult r2) {

        return r1.getParticipantName().compareToIgnoreCase(r2.getParticipantName());
    }

    protected static Comparator<RaceResult> penaliseDNF(final Comparator<? super RaceResult> base_comparator) {

        return (r1, r2) -> {

            if (!r1.canComplete() && r2.canComplete()) return 1;
            if (r1.canComplete() && !r2.canComplete()) return -1;

            return base_comparator.compare(r1, r2);
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

    public boolean areEqualPositionsAllowed() {

        // Dead heats allowed in overall results. Although an ordering is imposed at the finish,
        // this can't be relied on due to mass starts.
        return true;
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
    protected static void setPositionStrings(final List<? extends RaceResult> results, final boolean allow_equal_positions) {

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
    private static void recordEqualPositions(final List<? extends RaceResult> results, final int start_index, final int end_index) {

        final String position_string = STR."\{start_index + 1}=";

        for (int i = start_index; i <= end_index; i++)
            results.get(i).position_string = position_string;
    }

    /** Finds the highest index for which the performance is the same as the given index. */
    private static int getHighestIndexWithSamePerformance(final List<? extends RaceResult> results, final int start_index) {

        int highest_index_with_same_result = start_index;

        while (highest_index_with_same_result < results.size() - 1 &&
            results.get(highest_index_with_same_result).comparePerformanceTo(results.get(highest_index_with_same_result + 1)) == 0)

            highest_index_with_same_result++;

        return highest_index_with_same_result;
    }

    /** Gets all the results eligible for the given prize categories. */
    public List<RaceResult> getOverallResults(final List<PrizeCategory> prize_categories) {

        final Predicate<RaceResult> prize_category_filter = result -> race.getCategoryDetails().isResultEligibleInSomePrizeCategory(null, race.getNormalisation().gender_eligibility_map, ((SingleRaceResult)result).entry.participant.category, prize_categories);
        final List<RaceResult> results = overall_results.stream().
            filter(prize_category_filter).
            map(result -> (RaceResult) result).
            toList();
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

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
import org.grahamkirby.race_timing.common.Team;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;

import org.grahamkirby.race_timing_experimental.common.*;
import org.grahamkirby.race_timing_experimental.individual_race.IndividualRaceOutputCSV;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.grahamkirby.race_timing.common.Race.UNKNOWN_BIB_NUMBER;
import static org.grahamkirby.race_timing.common.output.RaceOutput.DNF_STRING;
import static org.grahamkirby.race_timing_experimental.common.Config.KEY_DNF_FINISHERS;
import static org.grahamkirby.race_timing_experimental.common.Normalisation.format;

public class RelayRaceResultsCalculatorImpl implements RaceResultsCalculator {

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private Race race;
    private RelayRaceImpl race_impl;

    private List<RaceResult> overall_results;
    private StringBuilder notes;
    /** Provides functionality for inferring missing bib number or timing data in the results. */
    private RelayRaceMissingData missing_data;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public void setRace(Race race) {
        this.race = race;
        race_impl = ((RelayRaceImpl) race.getSpecific());
        notes = new StringBuilder();
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
            forEachOrdered(result -> recordLegResult(result));
    }

    private void recordLegResult(final RawResult r) {

        final RelayRaceRawResult raw_result = (RelayRaceRawResult) r;
        final int team_index = findIndexOfTeamWithBibNumber(raw_result.getBibNumber());
        final RelayRaceResult result = (RelayRaceResult) overall_results.get(team_index);

        final int leg_index = findIndexOfNextUnfilledLegResult(result.leg_results);
        final LegResult leg_result = result.leg_results.get(leg_index);

        leg_result.finish_time = raw_result.getRecordedFinishTime().plus(race_impl.start_offset);

        // Leg number will be zero in most cases, unless explicitly recorded in raw results.
        leg_result.leg_number = raw_result.getLegNumber();

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

        for (int leg_index = 0; leg_index < race_impl.number_of_legs; leg_index++)
            fillLegResultDetails(((RelayRaceResult) result).leg_results, leg_index);
    }

    private void fillLegResultDetails(final List<? extends LegResult> leg_results, final int leg_index) {

        final LegResult leg_result = leg_results.get(leg_index);

        final Duration individual_start_time = getIndividualStartTime(leg_result, leg_index);
        final Duration leg_mass_start_time = race_impl.start_times_for_mass_starts.get(leg_index);
        final Duration previous_team_member_finish_time = leg_index > 0 ? leg_results.get(leg_index - 1).finish_time : null;

        leg_result.start_time = getLegStartTime(individual_start_time, leg_mass_start_time, previous_team_member_finish_time, leg_index);

        // Record whether the runner started in a mass start.
        leg_result.in_mass_start = isInMassStart(individual_start_time, leg_mass_start_time, previous_team_member_finish_time, leg_index);
    }

    private Duration getIndividualStartTime(final LegResult leg_result, final int leg_index) {

        return race_impl.individual_starts.stream().
            filter(individual_leg_start -> individual_leg_start.bib_number() == leg_result.entry.bib_number).
            filter(individual_leg_start -> individual_leg_start.leg_number() == leg_index + 1).
            map(individual_leg_start -> individual_leg_start.start_time()).
            findFirst().
            orElse(null);
    }

    List<String> getLegDetails(final RelayRaceResult result) {

        final List<String> leg_details = new ArrayList<>();
        boolean all_previous_legs_completed = true;

        for (int leg = 1; leg <= race_impl.number_of_legs; leg++) {

            final LegResult leg_result = result.leg_results.get(leg - 1);
            final boolean completed = leg_result.canComplete();

            final String leg_runner_names = ((Team)leg_result.entry.participant).runner_names.get(leg - 1);
            final String leg_mass_start_annotation = getMassStartAnnotation(leg_result, leg);
            final String leg_time = IndividualRaceOutputCSV.renderDuration(leg_result, DNF_STRING);
            final String split_time = completed && all_previous_legs_completed ? format(sumDurationsUpToLeg(result.leg_results, leg)) : DNF_STRING;

            leg_details.add(leg_runner_names + leg_mass_start_annotation);
            leg_details.add(leg_time);
            leg_details.add(split_time);

            if (!completed) all_previous_legs_completed = false;
        }

        return leg_details;
    }

    private static Duration sumDurationsUpToLeg(final List<? extends LegResult> leg_results, final int leg_number) {

        return leg_results.stream().
            limit(leg_number).
            map(LegResult::duration).
            reduce(Duration.ZERO, Duration::plus);
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
        if (previous_runner_finish_time == null) return race_impl.mass_start_legs.get(leg_index);

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

        List<RawResult> raw_results = race.getRaceData().getRawResults();
        for (int i = 0; i < raw_results.size(); i++) {

            final boolean last_electronically_recorded_result = i == race_impl.getNumberOfRawResults() - 1;

            if (last_electronically_recorded_result && race_impl.getNumberOfRawResults() < raw_results.size())
                raw_results.get(i).appendComment("Remaining times from paper recording sheet only.");
        }
    }

    @Override
    public StringBuilder getNotes() {
        return notes;
    }

    private void allocatePrizes() {

        for (final PrizeCategory category : race.getCategoryDetails().getPrizeCategories())
            setPrizeWinners(category);
    }

    /** Returns prize winners in given category. */
    public List<RaceResult> getPrizeWinners(final PrizeCategory prize_category) {

        final List<RaceResult> prize_results = overall_results.stream().
            filter(result -> result.categories_of_prizes_awarded.contains(prize_category)).
            map(result -> (RaceResult) result).
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

    protected boolean isPrizeWinner(final RaceResult r, final PrizeCategory prize_category) {

        RelayRaceResult result = (RelayRaceResult) r;
        return result.canComplete() &&
            isStillEligibleForPrize(result, prize_category) &&
//            result.isResultEligibleForPrizeCategory(prize_category);
        CategoryDetailsImpl.isResultEligibleForPrizeCategory(null, race.getNormalisation().gender_eligibility_map, result.entry.participant.category, prize_category);
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

        return new RelayRaceResult(race, getEntryWithBibNumber(bib_number), finish_time);
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


    String getMassStartAnnotation(final LegResult leg_result, final int leg_number) {

        // Adds e.g. "(M3)" after names of runner_names that started in leg 3 mass start.
        return leg_result.in_mass_start ? STR." (M\{getNextMassStartLeg(leg_number)})" : "";
    }

    private int getNextMassStartLeg(final int leg_number) {

        return leg_number +
            (int) race_impl.mass_start_legs.subList(leg_number - 1, race_impl.number_of_legs).stream().
                filter(is_mass_start -> !is_mass_start).
                count();
    }
    /** Sorts all results by relevant comparators. */
    protected void sortResults() {

        overall_results.sort(combineComparators(getComparators()));
    }

    public List<Comparator<RaceResult>> getComparators() {

        return List.of(
            ignoreIfBothResultsAreDNF(penaliseDNF(RelayRaceResultsCalculatorImpl::comparePerformance)),
            ignoreIfEitherResultIsDNF(this::compareRecordedPosition),
            RelayRaceResultsCalculatorImpl::compareRunnerLastName,
            RelayRaceResultsCalculatorImpl::compareRunnerFirstName);
    }

    /** Compares the given results on the basis of their finish positions. */
    private int compareRecordedPosition(final RaceResult r1, final RaceResult r2) {

        final int recorded_position1 = getRecordedPosition(((RelayRaceResult)(r1)).entry.bib_number);
        final int recorded_position2 = getRecordedPosition(((RelayRaceResult)(r2)).entry.bib_number);

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
    protected static int comparePerformance(final RaceResult r1, final RaceResult r2) {

        return r1.comparePerformanceTo(r2);
    }

    /** Compares two results based on alphabetical ordering of the runners' first names. */
    protected static int compareRunnerFirstName(final RaceResult r1, final RaceResult r2) {

        return Normalisation.getFirstName(r1.getParticipant().name).compareTo(Normalisation.getFirstName(r2.getParticipant().name));
    }

    /** Compares two results based on alphabetical ordering of the runners' last names. */
    protected static int compareRunnerLastName(final RaceResult r1, final RaceResult r2) {

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
            map(result -> (RelayRaceResult)result).
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

            final RelayRaceResult result = (RelayRaceResult) results.get(result_index);

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

    /** Gets all the results eligible for the given prize categories. */
    public List<RaceResult> getOverallResults(final List<PrizeCategory> prize_categories) {

//        final Predicate<IndividualRaceResult> prize_category_filter = result -> result.isResultEligibleInSomePrizeCategory(prize_categories);
        final Predicate<RaceResult> prize_category_filter = result -> CategoryDetailsImpl.isResultEligibleInSomePrizeCategory(null, race.getNormalisation().gender_eligibility_map, ((SingleRaceResult)result).entry.participant.category, prize_categories);
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

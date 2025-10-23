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
package org.grahamkirby.race_timing.relay_race;

import org.grahamkirby.race_timing.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.*;

import java.time.Duration;
import java.util.*;

import static org.grahamkirby.race_timing.common.Config.*;

public class RelayRaceResultsCalculator extends RaceResultsCalculator {

    private static final int UNKNOWN_LEG_NUMBER = 0;

    // TODO tidy treatment of category configuration files.
    // TODO integrate with category configuration files.
    private static final List<String> GENDER_ORDER = Arrays.asList("Open", "Women", "Mixed");

    @Override
    public boolean areEqualPositionsAllowed() {

        // Dead heats allowed in overall results. Although an ordering is imposed at the finish,
        // this can't be relied on due to mass starts.
        return true;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Provides functionality for inferring missing bib number or timing data in the results. */
    private RelayRaceMissingData missing_data;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void setRace(final Race race) {

        super.setRace(race);
        missing_data = new RelayRaceMissingData(race);
    }

    @Override
    public void calculateResults() {

        initialiseResults();

        missing_data.interpolateMissingTimes();
        missing_data.guessMissingBibNumbers();

        recordFinishTimes();
        recordStartTimes();
        recordDNFs();

        sortResults();
        allocatePrizes();

        addPaperRecordingComments();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void recordFinishTimes() {

        recordLegResults();
        sortLegResults();
    }

    private void recordLegResults() {

        race.getRawResults().stream().
            filter(result -> result.getBibNumber() != UNKNOWN_BIB_NUMBER).
            forEachOrdered(this::recordLegResult);
    }

    private void recordLegResult(final RawResult raw_result) {

        final int team_index = findIndexOfTeamWithBibNumber(raw_result.getBibNumber());
        final RelayRaceResult result = (RelayRaceResult) overall_results.get(team_index);

        final int leg_index = findIndexOfNextUnfilledLegResult(result.getLegResults());
        final RelayRaceLegResult leg_result = result.getLegResult(leg_index + 1);

        final Duration recorded_finish_time = raw_result.getRecordedFinishTime();
        final Duration start_offset = ((RelayRace) race).getStartOffset();

        leg_result.setFinishTime(recorded_finish_time.plus(start_offset));

        // Leg number will be zero in most cases, unless explicitly recorded in raw results.
        leg_result.setLegNumber(((RelayRace) race).explicitly_recorded_leg_numbers.getOrDefault(raw_result, UNKNOWN_LEG_NUMBER));

        // Provisionally this leg is not DNF since a finish time was recorded.
        // However, it might still be set to DNF in recordDNFs() if the runner missed a checkpoint.
        leg_result.setDnf(false);
    }

    private void sortLegResults() {

        overall_results.forEach(RelayRaceResultsCalculator::sortLegResults);
    }

    private static void sortLegResults(final RaceResult result) {

        final List<RelayRaceLegResult> leg_results = ((RelayRaceResult) result).getLegResults();

        // Sort by explicitly recorded leg number (most results will not have explicit leg number).
        leg_results.sort(Comparator.comparingInt(RelayRaceLegResult::getLegNumber));

        // Reset the leg numbers according to new positions in leg sequence.
        for (int leg_index = 1; leg_index <= leg_results.size(); leg_index++)
            leg_results.get(leg_index - 1).setLegNumber(leg_index);
    }

    private RelayRaceLegResult getLegResult(final int bib_number, final int leg_number) {

        final RelayRaceResult result = (RelayRaceResult) overall_results.get(findIndexOfTeamWithBibNumber(bib_number));

        return result.getLegResult(leg_number);
    }

    private void recordStartTimes() {

        overall_results.forEach(this::fillLegResultDetails);
    }

    private void fillLegResultDetails(final RaceResult result) {

        final List<RelayRaceLegResult> leg_results = ((RelayRaceResult) result).getLegResults();

        for (int leg_index = 0; leg_index < ((RelayRace) race).getNumberOfLegs(); leg_index++)
            fillLegResultDetails(leg_results, leg_index);
    }

    private void fillLegResultDetails(final List<? extends RelayRaceLegResult> leg_results, final int leg_index) {

        final RelayRaceLegResult leg_result = leg_results.get(leg_index);

        final Duration individual_start_time = getIndividualStartTime(leg_result, leg_index);
        final Duration leg_mass_start_time = ((RelayRace) race).getStartTimesForMassStarts().get(leg_index);
        final Duration previous_team_member_finish_time = leg_index > 0 ? leg_results.get(leg_index - 1).getFinishTime() : null;

        leg_result.start_time = getLegStartTime(leg_index, individual_start_time, leg_mass_start_time, previous_team_member_finish_time);

        // Record whether the runner started in a mass start.
        leg_result.setInMassStart(isInMassStart(individual_start_time, leg_mass_start_time, previous_team_member_finish_time, leg_index));
    }

    private Duration getIndividualStartTime(final RelayRaceLegResult leg_result, final int leg_index) {

        return ((RelayRace) race).getIndividualStarts().stream().
            filter(individual_leg_start -> individual_leg_start.bib_number() == leg_result.getBibNumber()).
            filter(individual_leg_start -> individual_leg_start.leg_number() == leg_index + 1).
            map(RelayRace.IndividualStart::start_time).
            findFirst().
            orElse(null);
    }

    private static Duration getLegStartTime(final int leg_index, final Duration individual_start_time, final Duration mass_start_time, final Duration previous_team_member_finish_time) {

        // Check whether individual leg start time is recorded for this runner.
        if (individual_start_time != null) return individual_start_time;

        // If there's no individual leg start time recorded (previous check), and this is a Leg 1 runner, start at time zero.
        if (leg_index == 0) return Duration.ZERO;

        // This is later leg runner. If there's no finish time recorded for previous runner, we can't deduce a start time for this one.
        // This leg result will be set to DNF by default.
        if (previous_team_member_finish_time == null) return null;

        // Use the earlier of the mass start time, if present, and the previous runner's finish time.
        return !mass_start_time.equals(VERY_LONG_DURATION) && mass_start_time.compareTo(previous_team_member_finish_time) < 0 ? mass_start_time : previous_team_member_finish_time;
    }

    private boolean isInMassStart(final Duration individual_start_time, final Duration mass_start_time, final Duration previous_runner_finish_time, final int leg_index) {

        // Not in mass start if there is an individually recorded time, or it's the first leg.
        if (individual_start_time != null || leg_index == 0) return false;

        // No previously recorded leg time, so record this runner as starting in mass start if it's a mass start leg.
        if (previous_runner_finish_time == null) return ((RelayRace) race).getMassStartLegs().get(leg_index);

        // Record this runner as starting in mass start if the previous runner finished after the relevant mass start.
        return !mass_start_time.equals(VERY_LONG_DURATION) && mass_start_time.compareTo(previous_runner_finish_time) < 0;
    }

    private static int findIndexOfNextUnfilledLegResult(final List<? extends RelayRaceLegResult> leg_results) {

        return (int) leg_results.stream().
            takeWhile(result -> result.getFinishTime() != null).
            count();
    }

    private int findIndexOfTeamWithBibNumber(final int bib_number) {

        return (int) overall_results.stream().
            map(result -> (RelayRaceResult)result).
            takeWhile(result -> result.getBibNumber() != bib_number).
            count();
    }

    private void addPaperRecordingComments() {

        final List<RawResult> raw_results = race.getRawResults();
        final int number_of_electronically_recorded_results = ((RelayRace) race).number_of_electronically_recorded_raw_results;

        // TODO add check for zero.
        if (number_of_electronically_recorded_results < raw_results.size())
            raw_results.get(number_of_electronically_recorded_results - 1).appendComment("Remaining times from paper recording sheet only.");
    }

    protected void allocatePrizes() {

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

        // TODO unify with RaceResultsCalculator. Need configuration option for whether 2/3 in open age category is preferred over 1st in older category.
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

    private void initialiseResults() {

        final Collection<Integer> bib_numbers_seen = new HashSet<>();

        overall_results = race.getRawResults().stream().
            filter(raw_result -> raw_result.getBibNumber() != 0).
            filter(raw_result -> bib_numbers_seen.add(raw_result.getBibNumber())).
            map(this::makeRaceResult).
            toList();

        overall_results = makeMutableCopy(overall_results);
    }

    private static List<RaceResult> makeMutableCopy(final List<? extends RaceResult> results) {
        return new ArrayList<>(results);
    }

    private RaceResult makeRaceResult(final RawResult raw_result) {

        final RaceEntry entry = getEntryWithBibNumber(raw_result.getBibNumber());
        return new RelayRaceResult(race, entry, null);
    }

    protected void recordDNF(final String dnf_specification) {

        try {
            // String of form "bib-number/leg-number"

            final String[] elements = dnf_specification.split("/");
            final int bib_number = Integer.parseInt(elements[0]);
            final int leg_number = Integer.parseInt(elements[1]);

            getLegResult(bib_number, leg_number).setDnf(true);

        } catch (final NumberFormatException e) {
            throw new RuntimeException(dnf_specification, e);
        }
    }

    private RaceEntry getEntryWithBibNumber(final int bib_number) {

        return race.getEntries().stream().
            filter(entry -> entry.bib_number == bib_number).
            findFirst().
            orElseThrow();
    }
}

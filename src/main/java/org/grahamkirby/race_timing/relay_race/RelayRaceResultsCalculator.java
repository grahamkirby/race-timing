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
import org.grahamkirby.race_timing.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing.common.*;

import java.time.Duration;
import java.util.*;

import static org.grahamkirby.race_timing.common.Config.KEY_RACE_START_TIME;
import static org.grahamkirby.race_timing.common.Config.UNKNOWN_BIB_NUMBER;

public class RelayRaceResultsCalculator extends RaceResultsCalculator {

    private static final int UNKNOWN_LEG_NUMBER = 0;

    // TODO tidy treatment of category configuration files.
    // TODO integrate with category configuration files.
    private static final List<String> GENDER_ORDER = Arrays.asList("Open", "Women", "Mixed");

    /** Provides functionality for inferring missing bib number or timing data in the results. */
    private final RelayRaceMissingData missing_data;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public RelayRaceResultsCalculator(final RaceInternal race) {

        super(race);
        missing_data = new RelayRaceMissingData((RelayRace) race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public RaceResults calculateResults() {

        initialiseResults();
        guessMissingData();

        recordFinishTimes();
        fillLegResultDetails();
        recordDNFs();

        sortOverallResults();
        allocatePrizes();

        addPaperRecordingComments();

        return makeRaceResults();
    }

    @Override
    public boolean areEqualPositionsAllowed() {

        // Dead heats allowed in overall results. Although an ordering is imposed at the finish,
        // this can't be relied on due to mass starts.
        return true;
    }

    @Override
    protected void allocatePrizes() {

        // Allocate first prize in each category first, in decreasing order of category breadth.
        // This is because e.g. a 40+ team should win first in 40+ category before a subsidiary
        // prize in open category.

        final List<PrizeCategory> categories_sorted_by_decreasing_generality = sortByDecreasingGenerality(race.getCategoriesProcessor().getPrizeCategories());

        allocateFirstPrizes(categories_sorted_by_decreasing_generality);
        allocateMinorPrizes(categories_sorted_by_decreasing_generality);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
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

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void initialiseResults() {

        final Collection<Integer> bib_numbers_seen = new HashSet<>();

        overall_results = ((SingleRaceInternal) race).getRawResults().stream().
            filter(raw_result -> raw_result.getBibNumber() != 0).
            filter(raw_result -> bib_numbers_seen.add(raw_result.getBibNumber())).
            map(this::makeRaceResult).
            toList();

        overall_results = makeMutableCopy(overall_results);
    }

    private void guessMissingData() {

        missing_data.interpolateMissingTimes();
        missing_data.guessMissingBibNumbers();
    }

    private void recordFinishTimes() {

        recordLegResults();
        sortLegResults();
    }

    private void recordLegResults() {

        ((SingleRaceInternal) race).getRawResults().stream().
            filter(result -> result.getBibNumber() != UNKNOWN_BIB_NUMBER).
            forEachOrdered(this::recordLegResult);
    }

    private void recordLegResult(final RawResult raw_result) {

        final int team_index = findIndexOfTeamWithBibNumber(raw_result.getBibNumber());
        final RelayRaceResult result = (RelayRaceResult) overall_results.get(team_index);

        final int leg_index = findIndexOfNextUnfilledLegResult(result.getLegResults());
        final RelayRaceLegResult leg_result = result.getLegResult(leg_index + 1);

        leg_result.setFinishTime(raw_result.getRecordedFinishTime());

        // Leg number will be unknown in most cases, unless explicitly recorded in raw results.
        leg_result.setLegNumber(((RelayRace) race).getExplicitlyRecordedLegNumbers().getOrDefault(raw_result, UNKNOWN_LEG_NUMBER));

        // Provisionally this leg is not DNF since a finish time was recorded.
        // However, it might still be set to DNF in recordDNF() if the runner missed a checkpoint.
        leg_result.setDnf(false);
    }

    private void sortLegResults() {

        overall_results.forEach(this::sortLegResults);
    }

    private void sortLegResults(final RaceResult result) {

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

    private void fillLegResultDetails() {

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

        final Duration start_time = getLegStartTime(individual_start_time, leg_mass_start_time, previous_team_member_finish_time, leg_index);
        final boolean in_mass_start = isInMassStart(individual_start_time, leg_mass_start_time, previous_team_member_finish_time, leg_index);

        leg_result.setStartTime(start_time);
        leg_result.setInMassStart(in_mass_start);
    }

    private Duration getIndividualStartTime(final RelayRaceLegResult leg_result, final int leg_index) {

        return ((RelayRace) race).getIndividualStarts().stream().
            filter(individual_leg_start -> individual_leg_start.bib_number() == leg_result.getBibNumber()).
            filter(individual_leg_start -> individual_leg_start.leg_number() == leg_index + 1).
            map(RelayRace.IndividualStart::start_time).
            findFirst().
            orElse(null);
    }

    private Duration getLegStartTime(final Duration individual_start_time, final Duration mass_start_time, final Duration previous_team_member_finish_time, final int leg_index) {

        Duration start_time;

        // Check whether individual leg start time is recorded for this runner.
        if (individual_start_time != null) start_time = individual_start_time;

        // If there's no individual leg start time recorded (previous check), and this is a first runner, start at time zero.
        else if (leg_index == 0) start_time = Duration.ZERO;

        // This is a later leg runner. If there's no finish time recorded for previous runner, we can't deduce a start time for this one.
        // This leg result will be set to DNF by default.
        else if (previous_team_member_finish_time == null) start_time =  null;

        // Use the earlier of the mass start time, if present, and the previous runner's finish time.
        else start_time = mass_start_time != null && mass_start_time.compareTo(previous_team_member_finish_time) < 0 ?
            mass_start_time :
            previous_team_member_finish_time;

        // Adjust start time for first leg runner if timing didn't start at zero.
        if (leg_index == 0) {

            // Get offset between actual race start time, and the time at which timing started.
            // Usually this is zero. A positive value indicates that the race started after timing started.
            final Duration race_start_time = (Duration) race.getConfig().get(KEY_RACE_START_TIME);

            start_time = start_time.plus(race_start_time);
        }

        return start_time;
    }

    private boolean isInMassStart(final Duration individual_start_time, final Duration mass_start_time, final Duration previous_runner_finish_time, final int leg_index) {

        // In mass start if it's not the first leg,  there is no individually recorded start time, and the previous runner did not finish by the time of the mass start.

        final boolean individual_start_time_is_set = individual_start_time != null;
        final boolean mass_start_time_is_set = mass_start_time != null;
        final boolean previous_runner_finish_time_is_set = previous_runner_finish_time != null;

        final boolean previous_runner_not_finished_by_mass_start = mass_start_time_is_set && (!previous_runner_finish_time_is_set || mass_start_time.compareTo(previous_runner_finish_time) < 0);
        final boolean first_leg = leg_index == 0;

        return !first_leg && !individual_start_time_is_set && previous_runner_not_finished_by_mass_start;
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

        final List<RawResult> raw_results = ((SingleRaceInternal) race).getRawResults();
        final int number_of_electronically_recorded_results = ((RelayRace) race).getNumberOfElectronicallyRecordedRawResults();

        // TODO add check for zero.
        if (number_of_electronically_recorded_results < raw_results.size())
            raw_results.get(number_of_electronically_recorded_results - 1).appendComment("Remaining times from paper recording sheet only.");
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

    private RaceResult makeRaceResult(final RawResult raw_result) {

        final RaceEntry entry = getEntryWithBibNumber(raw_result.getBibNumber());
        return new RelayRaceResult(race, entry, null);
    }

    private RaceEntry getEntryWithBibNumber(final int bib_number) {

        return ((SingleRaceInternal) race).getEntries().stream().
            filter(entry -> entry.bib_number == bib_number).
            findFirst().
            orElseThrow();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private RaceResults makeRaceResults() {

        return new RelayRaceResults() {

            @Override
            public Config getConfig() {
                return race.getConfig();
            }

            @Override
            public Normalisation getNormalisation() {
                return race.getNormalisation();
            }

            @Override
            public Notes getNotes() {
                return race.getNotes();
            }

            @Override
            public List<? extends RaceResult> getOverallResults() {
                return race.getResultsCalculator().getOverallResults();
            }

            @Override
            public List<? extends RaceResult> getOverallResults(final List<PrizeCategory> categories) {
                return race.getResultsCalculator().getOverallResults(categories);
            }

            @Override
            public List<? extends RaceResult> getPrizeWinners(final PrizeCategory category) {
                return race.getResultsCalculator().getPrizeWinners(category);
            }

            @Override
            public List<String> getTeamPrizes() {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<PrizeCategoryGroup> getPrizeCategoryGroups() {
                return race.getCategoriesProcessor().getPrizeCategoryGroups();
            }

            @Override
            public boolean arePrizesInThisOrLaterCategory(final PrizeCategory prizeCategory) {
                return race.getResultsCalculator().arePrizesInThisOrLaterCategory(prizeCategory);
            }

            //////////////////////////////////////////////////////////////////////////////////////////////////

            @Override
            public List<? extends RawResult> getRawResults() {
                return ((RelayRace) race).getRawResults();
            }

            @Override
            public int getNumberOfLegs() {
                return ((RelayRace) race).getNumberOfLegs();
            }

            @Override
            public List<RelayRaceLegResult> getLegResults(final int leg) {
                return ((RelayRace) race).getLegResults(leg);
            }

            @Override
            public List<String> getLegDetails(final RelayRaceResult result) {
                return ((RelayRace) race).getLegDetails(result);
            }

            @Override
            public List<Boolean> getPairedLegs() {
                return ((RelayRace) race).getPairedLegs();
            }

            @Override
            public Map<Integer, Integer> countLegsFinishedPerTeam() {
                return ((RelayRace) race).countLegsFinishedPerTeam();
            }

            @Override
            public Map<RawResult, Integer> getExplicitlyRecordedLegNumbers() {
                return ((RelayRace) race).getExplicitlyRecordedLegNumbers();
            }

            @Override
            public List<Integer> getBibNumbersWithMissingTimes(final Map<Integer, Integer> leg_finished_count) {
                return ((RelayRace) race).getBibNumbersWithMissingTimes(leg_finished_count);
            }

            @Override
            public List<Duration> getTimesWithMissingBibNumbers() {
                return ((RelayRace) race).getTimesWithMissingBibNumbers();
            }
        };
    }
}

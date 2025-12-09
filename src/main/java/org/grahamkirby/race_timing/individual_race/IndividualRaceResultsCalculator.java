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
package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.categories.EntryCategory;
import org.grahamkirby.race_timing.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.*;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.Normalisation.parseTime;
import static org.grahamkirby.race_timing.individual_race.IndividualRaceResults.RunnerPerformance;
import static org.grahamkirby.race_timing.individual_race.IndividualRaceResults.TeamPerformance;

public class IndividualRaceResultsCalculator extends RaceResultsCalculator {

    /*
       Calculations specific to a single individual race. Features/assumptions:

     * Each runner is optionally associated with a club.
     * Runners without a club will be listed as 'Unatt.' (unattached) in results.
     * Club names are normalised in results via a stored list of common alternatives.
     * Runner names are unique within a club but not across clubs.
     * Runner names are unique among unattached runners.
     * Results timing may start at a different time from the actual race start.
     * There are no dead heats in results, since an ordering is imposed at the finish even where recorded times are the same.
     * Input data includes either a file containing entry details plus a file containing recorded times and bib numbers,
       or a file containing combined entry details and results.
     * Start times for particular runners may be provided separately, supporting individual early starts and late starts.
     * Start times for particular runner categories may be provided separately.
       * Used in Minitour races to allow older categories
         to start slightly before younger categories, to reduce congestion.
     * Start times may be calculated based on bib number.
       * Used in Minitour and Tour races for time trials starting in waves.
     * Separately recorded finish times for particular runners may be provided.
     * Runners that start but do not finish will not be recorded in results.

     */

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static final int DUMMY_BIB_NUMBER = 0;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public IndividualRaceResultsCalculator(final RaceInternal race) {
        super(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public RaceResults calculateResults() {

        initialiseResults();
        adjustTimes();
        addSeparatelyRecordedTimes();
        recordDNFs();
        sortOverallResults();
        allocatePrizes();

        getPrizeWinners(null);

        return makeRaceResults();
    }

    @Override
    public boolean canDistinguishFromOtherEqualPerformances(final RaceResult result) {

        // Normally results with same recorded time are not treated as dead heats, since an ordering is imposed at the
        // finish, but a dead heat can be recorded explicitly.
        return !((IndividualRace) race).getDeadHeats().contains(((IndividualRaceResult) result).getBibNumber());
    }

    @Override
    protected void recordDNF(final String dnf_specification) {

        final int bib_number = Integer.parseInt(dnf_specification);
        final SingleRaceResult result = (SingleRaceResult) getResultWithBibNumber(bib_number);

        result.setDnf(true);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Gets the median finish time for the race. */
    public Duration getMedianTime() {

        // The median time may be recorded explicitly if not all results are recorded.
        final String median_time_string = race.getConfig().getString(KEY_MEDIAN_TIME);
        if (median_time_string != null) return parseTime(median_time_string);

        return getOverallResults().size() % 2 == 0 ?
            getMedianTimeForEvenNumberOfResults() :
            getMedianTimeForOddNumberOfResults();
    }

    public int getGenderPosition(final String runner_name, final String club, final String gender) {

        return (int) getGenderResults(gender).stream().
            map(result -> (Runner) result.getParticipant()).
            takeWhile(runner -> !(runner.getName().equals(runner_name) && runner.getClub().equals(club))).
            count() + 1;
    }

    public List<SingleRaceResult> getGenderResults(final String gender) {

        return getOverallResults().stream().
            map(result -> (SingleRaceResult) result).
            filter(SingleRaceResult::canComplete).
            filter(result -> result.getCategory().getGender().equals(gender)).
            toList();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    // These are cached to avoid details being repeatedly written to notes for each output format.
    private List<TeamPerformance> team_prizes = null;

    private synchronized List<TeamPerformance> getTeamPrizes() {

        if (team_prizes == null) {

            team_prizes = race.getConfig().containsKey(KEY_TEAM_PRIZE_GENDER_CATEGORIES) ?

                Arrays.stream(race.getConfig().getString(KEY_TEAM_PRIZE_GENDER_CATEGORIES).split("/")).
                    map(this::getFirstTeamInGenderCategory).
                    filter(Optional::isPresent).
                    map(Optional::get).
                    toList() :

                List.of();
        }

        return team_prizes;
    }

    private void initialiseResults() {

        final List<RaceResult> directly_recorded_results = ((SingleRaceInternal) race).getOverallResults();
        final List<RawResult> raw_results = ((SingleRaceInternal) race).getRawResults();

        overall_results = makeMutableCopy(
            !directly_recorded_results.isEmpty() ? directly_recorded_results : getRaceResults(raw_results));
    }

    private List<RaceResult> getRaceResults(final List<RawResult> raw_results) {

        return raw_results.stream().
            map(this::makeRaceResult).
            toList();
    }

    private RaceResult makeRaceResult(final RawResult raw_result) {

        final int bib_number = raw_result.getBibNumber();
        final Duration finish_time = raw_result.getRecordedFinishTime();

        return new IndividualRaceResult(getEntryWithBibNumber(bib_number), finish_time, race);
    }

    private RaceEntry getEntryWithBibNumber(final int bib_number) {

        return ((SingleRaceInternal) race).getEntries().stream().
            filter(entry -> entry.getBibNumber() == bib_number).
            findFirst().
            orElseThrow();
    }

    private RaceResult getResultWithBibNumber(final int bib_number) {

        return overall_results.stream().
            map(result -> (SingleRaceResult) result).
            filter(result -> result.getBibNumber() == bib_number).
            findFirst().
            orElseThrow();
    }

    private void adjustTimes() {

        setTimesByCategory();
        setIndividualStartTimes();
        setTimeTrialStartTimes();
    }

    private void addSeparatelyRecordedTimes() {

        final Map<Integer, Duration> separately_recorded_finish_times = ((IndividualRace) race).getSeparatelyRecordedFinishTimes();

        for (final Map.Entry<Integer, Duration> entry : separately_recorded_finish_times.entrySet())
            overall_results.add(makeRaceResult(new RawResult(entry.getKey(), entry.getValue())));
    }

    private void setTimesByCategory() {

        // Category / start time
        // Example: CATEGORY_START_OFFSETS =  FU9/00:01:00,MU9/00:01:00,FU11/00:01:00,MU11/00:01:00

        final Consumer<Object> process_category_start_times = category_start_offsets -> {

            final Map<EntryCategory, Duration> category_offsets = new HashMap<>();

            for (final String offset_string : ((String) category_start_offsets).split(",", -1)) {

                final String[] split = offset_string.split("/");
                category_offsets.put(race.getCategoriesProcessor().getEntryCategory(split[0]), parseTime(split[1]));
            }

            for (final RaceResult r : overall_results) {

                final SingleRaceResult result = (SingleRaceResult) r;
                final EntryCategory category = result.getParticipant().getCategory();

                final Duration category_start_time = category_offsets.get(category);

                if (category_start_time != null)
                    result.setStartTime(category_start_time);
            }
        };

        race.getConfig().processConfigIfPresent(KEY_CATEGORY_START_OFFSETS, process_category_start_times);
    }

    private void setTimeTrialStartTimes() {

        // This option applies when time-trial runners are assigned to waves in order of bib number,
        // with incomplete waves if there are any gaps in bib numbers.

        final Consumer<Object> process_time_trial_start_times = time_trial_runners_per_wave -> {

            final Duration time_trial_inter_wave_interval = (Duration) race.getConfig().get(KEY_TIME_TRIAL_INTER_WAVE_INTERVAL);

            for (final RaceResult r : overall_results) {

                final SingleRaceResult result = (SingleRaceResult) r;
                final int wave_number = (result.getBibNumber() - 1) / ((int) time_trial_runners_per_wave);

                result.setStartTime(time_trial_inter_wave_interval.multipliedBy(wave_number));
            }
        };

        race.getConfig().processConfigIfPresent(KEY_TIME_TRIAL_RUNNERS_PER_WAVE, process_time_trial_start_times);
    }

    private void setIndividualStartTimes() {

        // Bib number / start time
        // Example: INDIVIDUAL_START_TIMES = 2/0:10:00,26/0:20:00

        final Consumer<Object> process_individual_start_times = individual_start_times -> {

            final Map<Integer, Duration> start_times = new HashMap<>();

            for (final String individual_early_start : ((String) individual_start_times).split(",")) {

                final String[] split = individual_early_start.split("/");

                final int bib_number = Integer.parseInt(split[0]);
                final Duration offset = parseTime(split[1]);

                start_times.put(bib_number, offset);
            }

            for (final RaceResult r : overall_results) {

                final SingleRaceResult result = (SingleRaceResult) r;

                if (start_times.containsKey(result.getBibNumber()))
                    result.setStartTime(start_times.get(result.getBibNumber()));
            }
        };

        race.getConfig().processConfigIfPresent(KEY_INDIVIDUAL_START_TIMES, process_individual_start_times);
    }

    private Duration getMedianTimeForOddNumberOfResults() {

        final List<RaceResult> results = getOverallResults();

        final SingleRaceResult median_result = (SingleRaceResult) results.get(results.size() / 2);
        return (Duration) median_result.getPerformance().getValue();
    }

    private Duration getMedianTimeForEvenNumberOfResults() {

        final List<RaceResult> results = getOverallResults();

        final SingleRaceResult median_result1 = (SingleRaceResult) results.get(results.size() / 2 - 1);
        final SingleRaceResult median_result2 = (SingleRaceResult) results.get(results.size() / 2);

        final Duration duration1 = (Duration) median_result1.getPerformance().getValue();
        final Duration duration2 = (Duration) median_result2.getPerformance().getValue();

        return duration1.plus(duration2).dividedBy(2);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    static int getAggregatePosition(final TeamPerformance performance) {

        return performance.runner_performances().stream().
            map(IndividualRaceResults.RunnerPerformance::position).
            reduce(0, Integer::sum);
    }

    private TeamPerformance getTeamPerformance(final String club, final String gender) {

        final int number_to_count_for_team_prize = (int) race.getConfig().get(KEY_TEAM_PRIZE_NUMBER_TO_COUNT);

        final List<RunnerPerformance> runner_names = getOverallResults().stream().
            map(result -> (Runner) result.getParticipant()).
            filter(runner -> runner.getClub().equals(club)).
            filter(runner -> runner.getCategory().getGender().equals(gender)).
            limit(number_to_count_for_team_prize).
            map(runner -> new RunnerPerformance(runner.getName(), getGenderPosition(runner.getName(), club, gender))).
            toList();

        return new TeamPerformance(club, gender, runner_names);
    }

    private Optional<TeamPerformance> getFirstTeamInGenderCategory(final String team_prize_gender_category) {

        final int number_to_count_for_team_prize = (int) race.getConfig().get(KEY_TEAM_PRIZE_NUMBER_TO_COUNT);

        // If aggregate positions are the same, use the first position as tie break.
        final Comparator<TeamPerformance> sort_by_aggregate_position = Comparator.comparingInt(IndividualRaceResultsCalculator::getAggregatePosition);
        final Comparator<TeamPerformance> sort_by_first_position = Comparator.comparingInt(p -> p.runner_performances().getFirst().position());

//        // Not necessary to sort clubs, but this makes it easier to reason about testing tie break.
//        return getClubs().stream().sorted().
//            map(club -> getTeamPerformance(club, team_prize_gender_category)).
//            filter(performance -> performance.runner_performances().size() >= number_to_count_for_team_prize).
//            min(sort_by_aggregate_position.thenComparing(sort_by_first_position));


        // Not necessary to sort clubs, but this makes it easier to reason about testing tie break.
        List<TeamPerformance> list = getClubs().stream().sorted().
            map(club -> getTeamPerformance(club, team_prize_gender_category)).
            filter(performance -> performance.runner_performances().size() >= number_to_count_for_team_prize).
            sorted(sort_by_aggregate_position.thenComparing(sort_by_first_position)).toList();

        Notes notes = race.getNotes();
        notes.appendToNotes("Team scores: " + team_prize_gender_category + LINE_SEPARATOR + LINE_SEPARATOR);

        for (int i = 0; i < list.size(); i++) {
            TeamPerformance performance = list.get(i);
            notes.appendToNotes(i + 1 + " ");
            notes.appendToNotes(performance.club());

            int team_score = performance.runner_performances().stream().mapToInt(run -> run.position()).sum();

            notes.appendToNotes(" " + team_score + " (");

            String individual_scores = performance.runner_performances().stream().map(run -> String.valueOf(run.position())).collect(Collectors.joining(", "));
            notes.appendToNotes(individual_scores + ")" + LINE_SEPARATOR);

        }

        notes.appendToNotes(LINE_SEPARATOR);

        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    private Set<String> getClubs() {

        return getOverallResults().stream().
            map(result -> ((Runner) result.getParticipant()).getClub()).
            filter(Predicate.not(club -> club.equals("Unatt."))).
            collect(Collectors.toSet());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected RaceResults makeRaceResults() {

        return new IndividualRaceResults() {

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
                return IndividualRaceResultsCalculator.this.overall_results;
            }

            @Override
            public List<? extends RaceResult> getOverallResults(final List<PrizeCategory> categories) {
                return IndividualRaceResultsCalculator.this.getOverallResults(categories);
            }

            @Override
            public List<? extends RaceResult> getPrizeWinners(final PrizeCategory category) {
                return IndividualRaceResultsCalculator.this.getPrizeWinners(category);
            }

            @Override
            public List<TeamPerformance> getTeamPrizes() {
                return IndividualRaceResultsCalculator.this.getTeamPrizes();
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
            public boolean arePrizesInThisOrLaterCategory(final PrizeCategory prizeCategory) {
                return IndividualRaceResultsCalculator.this.arePrizesInThisOrLaterCategory(prizeCategory);
            }
        };
    }
}

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
import org.grahamkirby.race_timing.common.*;

import java.time.Duration;
import java.util.*;

import static org.grahamkirby.race_timing.common.Config.*;

public class IndividualRaceImpl implements SpecificRace {

    private Race race;
    Map<EntryCategory, Duration> category_start_offsets;
    Map<Integer, Duration> individual_start_offsets;
    Map<Integer, Duration> time_trial_start_offsets;
    Map<Integer, Duration> separately_recorded_finish_times;

    int time_trial_runners_per_wave;
    Duration time_trial_inter_wave_interval;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public void setRace(Race race) {
        this.race = race;
    }

    @Override
    public void completeConfiguration() {

        category_start_offsets = readCategoryStartOffsets();
        individual_start_offsets = loadIndividualStartOffsets();
        time_trial_start_offsets = loadTimeTrialStarts();
        separately_recorded_finish_times = readSeparatelyRecordedResults();
    }

    private Map<EntryCategory, Duration> readCategoryStartOffsets() {

        final Map<EntryCategory, Duration> category_offsets = new HashMap<>();

        if (race.getConfig().containsKey(KEY_CATEGORY_START_OFFSETS)) {

            // e.g. FU9/00:01:00,MU9/00:01:00,FU11/00:01:00,MU11/00:01:00
            final String[] offset_strings = ((String) (race.getConfig().get(KEY_CATEGORY_START_OFFSETS))).split(",", -1);

            for (final String offset_string : offset_strings) {

                final String[] split = offset_string.split("/");
                category_offsets.put(race.getCategoryDetails().lookupEntryCategory(split[0]), Normalisation.parseTime(split[1]));
            }
        }

        return category_offsets;
    }

    private Map<Integer, Duration> readSeparatelyRecordedResults() {

        final Map<Integer, Duration> results = new HashMap<>();

        if (race.getConfig().containsKey(KEY_SEPARATELY_RECORDED_RESULTS)) {

            final String[] self_timed_strings = ((String) (race.getConfig().get(KEY_SEPARATELY_RECORDED_RESULTS))).split(",", -1);

            // SEPARATELY_RECORDED_RESULTS = 126/8:09

            for (final String s : self_timed_strings) {

                final String[] split = s.split("/", -1);
                final int bib_number = Integer.parseInt(split[0]);
                final Duration finish_time = Normalisation.parseTime(split[1]);

                results.put(bib_number, finish_time);
            }
        }

        return results;
    }

    private Map<Integer, Duration> loadTimeTrialStarts() {

        final Map<Integer, Duration> starts = new HashMap<>();

        // The first option applies when time-trial runners are assigned to waves in order of bib number,
        // with incomplete waves if there are any gaps in bib numbers.
        // The second option applies when start order is manually determined (e.g. to start current leaders first or last).

        if (race.getConfig().containsKey(KEY_TIME_TRIAL_RUNNERS_PER_WAVE)) {

            time_trial_runners_per_wave = (int) race.getConfig().get(KEY_TIME_TRIAL_RUNNERS_PER_WAVE);
            time_trial_inter_wave_interval = (Duration) race.getConfig().get(KEY_TIME_TRIAL_INTER_WAVE_INTERVAL);

            for (final RawResult raw_result : race.getRaceData().getRawResults()) {

                final int wave_number = runnerIndexInBibOrder(raw_result.getBibNumber()) / time_trial_runners_per_wave;
                final Duration start_offset = time_trial_inter_wave_interval.multipliedBy(wave_number);

                starts.put(raw_result.getBibNumber(), start_offset);
            }
        }

        if (race.getConfig().containsKey(KEY_TIME_TRIAL_STARTS)) {

            for (final String part : ((String) race.getConfig().get(KEY_TIME_TRIAL_STARTS)).split(",", -1)) {

                final String[] split = part.split("/");
                starts.put(Integer.parseInt(split[0]), Normalisation.parseTime(split[1]));
            }
        }

        return starts;
    }

    private Map<Integer, Duration> loadIndividualStartOffsets() {

        final Map<Integer, Duration> starts = new HashMap<>();

        final String individual_early_starts_string = (String) race.getConfig().get(KEY_INDIVIDUAL_EARLY_STARTS);

        // bib number / start time difference
        // Example: INDIVIDUAL_EARLY_STARTS = 2/0:10:00,26/0:20:00

        if (individual_early_starts_string != null)
            for (final String individual_early_start : individual_early_starts_string.split(",")) {

                final String[] split = individual_early_start.split("/");

                final int bib_number = Integer.parseInt(split[0]);
                final Duration offset = Normalisation.parseTime(split[1]);

                // Offset will be subtracted from recorded time,
                // so negate since a positive time corresponds to an early start.

                starts.put(bib_number, offset.negated());
            }

        return starts;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static int runnerIndexInBibOrder(final int bib_number) {
        return bib_number - 1;
    }

    public List<String> getTeamPrizes() {

        final List<RaceResult> overall_results = race.getResultsCalculator().getOverallResults();
        final Set<String> clubs = getClubs(overall_results);

        int best_male_team_total = Integer.MAX_VALUE;
        String best_male_team = "";
        int best_female_team_total = Integer.MAX_VALUE;
        String best_female_team = "";

        for (final String club : clubs) {

            final int male_team_total = getTeamTotal(club, "Men");
            final int female_team_total = getTeamTotal(club, "Women");

            if (male_team_total < best_male_team_total) {
                best_male_team = club;
                best_male_team_total = male_team_total;
            }

            if (female_team_total < best_female_team_total) {
                best_female_team = club;
                best_female_team_total = female_team_total;
            }
        }

        final List<String> prizes = new ArrayList<>();

        if (best_male_team_total < Integer.MAX_VALUE)
            prizes.add("First male team: " + best_male_team + " (" + best_male_team_total + ")");

        if (best_female_team_total < Integer.MAX_VALUE)
            prizes.add("First female team: " + best_female_team + " (" + best_female_team_total + ")");

        return prizes;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private int getTeamTotal(final String club, final String gender) {

        int number_to_count_for_team_prize = (Integer) race.getConfig().get(KEY_NUMBER_TO_COUNT_FOR_TEAM_PRIZE);

        int result_position = 0;
        int team_count = 0;
        int total = 0;

        for (final RaceResult result : race.getResultsCalculator().getOverallResults()) {

            result_position++;

            final Runner runner = (Runner) ((SingleRaceResult) result).entry.participant;

            if (team_count < number_to_count_for_team_prize && runner.club.equals(club) && runner.category.getGender().equals(gender)) {
                team_count++;
                total += result_position;
            }
        }

        return team_count >= number_to_count_for_team_prize ? total : Integer.MAX_VALUE;
    }

    private Set<String> getClubs(final List<RaceResult> results) {

        final Set<String> clubs = new HashSet<>();
        for (final RaceResult result : results) {

            final String club = ((Runner) ((SingleRaceResult) result).entry.participant).club;
            clubs.add(club);
        }
        return clubs;
    }
}

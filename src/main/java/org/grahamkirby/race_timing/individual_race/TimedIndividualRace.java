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
package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.common.RaceInput;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.single_race.SingleRaceResult;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

import static org.grahamkirby.race_timing.common.Normalisation.parseTime;
import static org.grahamkirby.race_timing_experimental.common.Config.KEY_INDIVIDUAL_EARLY_STARTS;
import static org.grahamkirby.race_timing_experimental.common.Config.KEY_NUMBER_TO_COUNT_FOR_TEAM_PRIZE;

public class TimedIndividualRace extends TimedRace {

    int number_to_count_for_team_prize = Integer.MAX_VALUE;

    public TimedIndividualRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    @SuppressWarnings("MethodOverridesStaticMethodOfSuperclass")
    public static void main(final String[] args) throws Exception {

        commonMain(args, config_file_path -> new TimedIndividualRace(Path.of(config_file_path)));
    }

    @Override
    protected RaceInput getInput() {
        return new TimedIndividualRaceInput(this);
    }

    @Override
    public void calculateResults() {

        initialiseResults();
        configureTeamPrizes();
        configureIndividualEarlyStarts();
        super.calculateResults();
    }

    @Override
    public List<String> getTeamPrizes() {

        final List<RaceResult> overall_results = getOverallResults();
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

    private int getTeamTotal(final String club, final String gender) {

        int result_position = 0;
        int team_count = 0;
        int total = 0;

        for (final RaceResult result : getOverallResults()) {

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

    @Override
    protected void outputResults() throws IOException {

        super.outputResults();
    }

    private void initialiseResults() {

        overall_results = raw_results.stream().
            map(this::makeResult).
            toList();

        overall_results = makeMutable(overall_results);
    }

    private RaceResult makeResult(final RawResult raw_result) {

        final int bib_number = raw_result.getBibNumber();
        final Duration finish_time = raw_result.getRecordedFinishTime();

        return new SingleRaceResult(this, getEntryWithBibNumber(bib_number), finish_time);
    }

    private void configureTeamPrizes() {

        final String number_to_count_for_team_prize_string = getOptionalProperty(KEY_NUMBER_TO_COUNT_FOR_TEAM_PRIZE);

        if (number_to_count_for_team_prize_string != null)
            number_to_count_for_team_prize = Integer.parseInt(number_to_count_for_team_prize_string);
    }

    private void configureIndividualEarlyStarts() {

        final String individual_early_starts_string = getOptionalProperty(KEY_INDIVIDUAL_EARLY_STARTS);

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

        final SingleRaceResult result = getResultByBibNumber(bib_number);

        result.finish_time = result.finish_time.plus(offset);
    }

    private SingleRaceResult getResultByBibNumber(final int bib_number) {

        return (SingleRaceResult) overall_results.stream().
            filter(result -> ((SingleRaceResult) result).entry.bib_number == bib_number).
            findFirst().
            orElseThrow();
    }
}

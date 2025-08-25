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
package org.grahamkirby.race_timing_experimental.individual_race;

import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing_experimental.common.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IndividualRaceImpl implements SpecificRace {

    private Race race;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public void setRace(Race race) {
        this.race = race;
    }

    @Override
    public void completeConfiguration() {
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

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

        int number_to_count_for_team_prize = (Integer) race.getConfig().get(Config.KEY_NUMBER_TO_COUNT_FOR_TEAM_PRIZE);

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

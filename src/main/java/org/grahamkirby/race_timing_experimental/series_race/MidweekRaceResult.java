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


import org.grahamkirby.race_timing.common.Participant;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing_experimental.common.Config;
import org.grahamkirby.race_timing_experimental.common.Race;
import org.grahamkirby.race_timing_experimental.common.RaceResult;
import org.grahamkirby.race_timing_experimental.common.SingleRaceResult;

import java.util.List;
import java.util.Objects;

class MidweekRaceResult extends RaceResult {

    final Runner runner;
    private final List<Integer> scores;

    MidweekRaceResult(final Runner runner, final List<Integer> scores, final Race race) {

        super(race);
        this.runner = runner;
        this.scores = scores;
    }

    @Override
    public String getParticipantName() {
        return runner.name;
    }

    @Override
    public Participant getParticipant() {
        return runner;
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {

        // Negate so that a higher score gives an earlier ranking.
        return -Integer.compare(totalScore(), ((MidweekRaceResult) other).totalScore());
    }

    int totalScore() {

        final int minimum_number_of_races = Integer.parseInt((String) race.getConfig().get(Config.KEY_MINIMUM_NUMBER_OF_RACES));
        final int number_of_races_completed = numberOfRacesCompleted();
        final int number_of_counting_scores = Math.min(minimum_number_of_races, number_of_races_completed);

        return -scores.stream().
            map(score -> -score).
            sorted().
            limit(number_of_counting_scores).
            reduce(0, Integer::sum);
    }

    @Override
    public EntryCategory getCategory() {
        return runner.category;
    }

    @Override
    public boolean canComplete() {

        final int number_of_races_remaining = Integer.parseInt((String) race.getConfig().get(Config.KEY_NUMBER_OF_RACES_IN_SERIES)) - ((MidweekRaceImpl) race.getSpecific()).getNumberOfRacesTakenPlace();

        return numberOfRacesCompleted() + number_of_races_remaining >= Integer.parseInt((String) race.getConfig().get(Config.KEY_MINIMUM_NUMBER_OF_RACES));
    }

    @Override
    public boolean shouldDisplayPosition() {

        return canComplete();
    }

    public boolean hasCompletedSeries() {

        return numberOfRacesCompleted() >= Integer.parseInt((String) race.getConfig().get(Config.KEY_MINIMUM_NUMBER_OF_RACES));
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected int numberOfRacesCompleted() {

        return (int) ((MidweekRaceImpl)race.getSpecific()).getRaces().stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getResultsCalculator().getOverallResults().stream()).
            map(result -> (SingleRaceResult) result).
            filter(result -> result.entry.participant.equals(runner)).
            filter(SingleRaceResult::canComplete).
            count();
    }
}

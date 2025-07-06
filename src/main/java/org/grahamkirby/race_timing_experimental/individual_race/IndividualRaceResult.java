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

import org.grahamkirby.race_timing.common.Participant;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing_experimental.common.Race;

import java.time.Duration;
import java.util.*;

public class IndividualRaceResult {

    public IndividualRaceEntry entry;
    public Duration finish_time;
    public boolean dnf;
    public Race race;
    public String position_string;
    List<PrizeCategory> categories_of_prizes_awarded = new ArrayList<>();

    public IndividualRaceResult(final Race race, final IndividualRaceEntry entry, final Duration finish_time) {

        this.race = race;
        this.entry = entry;
        this.finish_time = finish_time;
    }

    protected String getParticipantName() {

        return entry.participant.name;
    }

    public boolean shouldDisplayPosition() {
        return canComplete();
    }

    protected String getClub() {

        // The participant field could hold a team rather than a runner, but this method is overridden in RelayRaceResult.
        return ((Runner) entry.participant).club;
    }

    public Duration duration() {
        return finish_time;
    }

    public int comparePerformanceTo(final IndividualRaceResult other) {

        final Duration duration = duration();
        final Duration other_duration = other.duration();

        return Comparator.nullsLast(Duration::compareTo).compare(duration, other_duration);
    }

    public EntryCategory getCategory() {
        return entry.participant.category;
    }

    public boolean canComplete() {
        return !dnf;
    }

    public Participant getParticipant() {
        return entry.participant;
    }
}

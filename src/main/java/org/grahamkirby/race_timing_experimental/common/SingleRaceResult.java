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
package org.grahamkirby.race_timing_experimental.common;


import org.grahamkirby.race_timing.common.Participant;
import org.grahamkirby.race_timing.common.categories.EntryCategory;

import java.time.Duration;
import java.util.Comparator;

public class SingleRaceResult extends RaceResult {

    public RaceEntry entry;
    public Duration finish_time;
    public boolean dnf;

    public SingleRaceResult(final Race race, final RaceEntry entry, final Duration finish_time) {

        super(race);
        this.entry = entry;
        this.finish_time = finish_time;
    }

    @Override
    protected String getParticipantName() {

        return entry.participant.name;
    }

    @Override
    public boolean shouldDisplayPosition() {
        return canComplete();
    }

    public Duration duration() {
        return finish_time;
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {

        final Duration duration = duration();
        final Duration other_duration = ((SingleRaceResult) other).duration();

        return Comparator.nullsLast(Duration::compareTo).compare(duration, other_duration);
    }

    @Override
    public EntryCategory getCategory() {
        return entry.participant.category;
    }

    @Override
    public boolean canComplete() {
        return !dnf;
    }


    @Override
    public Participant getParticipant() {
        return entry.participant;
    }
}

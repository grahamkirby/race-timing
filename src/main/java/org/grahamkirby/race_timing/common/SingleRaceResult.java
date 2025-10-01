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
package org.grahamkirby.race_timing.common;


import java.time.Duration;

public abstract class SingleRaceResult extends CommonRaceResult implements RaceResultWithDuration {

    public Duration finish_time;
    public boolean dnf;
    public int bib_number;

    public SingleRaceResult(final Race race, final RaceEntry entry, final Duration finish_time) {

        super(race, entry.participant);

        bib_number = entry.bib_number;
        this.finish_time = finish_time;
    }

    public Duration duration() {
        return finish_time;
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {

        final Duration duration = duration();
        final Duration other_duration = ((SingleRaceResult) other).duration();

        return duration.compareTo(other_duration);
    }

    @Override
    public boolean canComplete() {
        return !dnf;
    }
}

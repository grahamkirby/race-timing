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

    // TODO add start time.
    protected Duration finish_time;
    protected boolean dnf;
    protected int bib_number;

    public SingleRaceResult(final Race2 race, final RaceEntry entry, final Duration finish_time) {

        super(race, entry.participant);

        bib_number = entry.bib_number;
        this.finish_time = finish_time;
    }

    public Duration duration() {
        return finish_time;
    }

    public Duration getFinishTime() {
        return finish_time;
    }

    public void setFinishTime(final Duration finish_time) {
        this.finish_time = finish_time;
    }

    public int getBibNumber() {
        return bib_number;
    }

    public boolean isDnf() {
        return dnf;
    }

    public void setDnf(final boolean dnf) {
        this.dnf = dnf;
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

    /** Compares the given results on the basis of their finish positions. */
    protected int compareRecordedPosition(final RaceResult r1, final RaceResult r2) {

        if (r1.getRace() != r2.getRace())
            throw new RuntimeException("results compared from two different races");

        final int recorded_position1 = getRecordedPosition(((SingleRaceResult) r1).bib_number);
        final int recorded_position2 = getRecordedPosition(((SingleRaceResult) r2).bib_number);

        return Integer.compare(recorded_position1, recorded_position2);
    }

    private int getRecordedPosition(final int bib_number) {

        return (int) race.getRawResults().stream().
            takeWhile(result -> result.getBibNumber() != bib_number).
            count() + 1;
    }
}

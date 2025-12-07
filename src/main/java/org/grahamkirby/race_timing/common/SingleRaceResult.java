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
import java.util.Comparator;

public abstract class SingleRaceResult extends CommonRaceResult {

    protected Duration start_time;
    protected Duration finish_time;

    private boolean dnf;
    private final int bib_number;

    public SingleRaceResult(final RaceInternal race, final RaceEntry entry, final Duration finish_time) {

        super(race, entry.participant);

        bib_number = entry.bib_number;
        this.finish_time = finish_time;
        start_time = Duration.ZERO;
    }

    public Performance getPerformance() {

        return canComplete() ?  new DurationPerformance(finish_time.minus(start_time)) : null;
    }

    public void setStartTime(final Duration start_time) {
        this.start_time = start_time;
    }

    public Duration getFinishTime() {
        return finish_time;
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

        final Performance duration = getPerformance();
        final Performance other_duration = other.getPerformance();

        return Comparator.nullsLast(Performance::compareTo).compare(duration, other_duration);
    }

    @Override
    public boolean canComplete() {
        return !dnf;
    }

    /** Compares the given results on the basis of their finish positions. */
    protected static int compareRecordedPosition(final RaceResult r1, final RaceResult r2) {

//        int bib1 = ((SingleRaceResult) r1).bib_number;
//        int bib2 = ((SingleRaceResult) r2).bib_number;
//
//        if (r1.getRace() != r2.getRace())
//            throw new RuntimeException("results compared from two different races");
//
//        final int recorded_position1 = getRecordedPosition(((SingleRaceResult) r1).bib_number, (SingleRaceInternal) r1.getRace());
//        final int recorded_position2 = getRecordedPosition(((SingleRaceResult) r2).bib_number, (SingleRaceInternal) r1.getRace());
//
//        if (bib1==58 && bib2==94 || bib1==94 && bib2==58) {
//            int x = 3;
//        }
//
//        return Integer.compare(recorded_position1, recorded_position2);

        return ((SingleRaceResult)r1).compareRecordedPositionTo(r2);
    }

    protected int compareRecordedPositionTo(final RaceResult other) {

        int bib1 = ((SingleRaceResult) this).bib_number;
        int bib2 = ((SingleRaceResult) other).bib_number;

        if (this.getRace() != other.getRace())
            throw new RuntimeException("results compared from two different races");

        final int recorded_position1 = getRecordedPosition(((SingleRaceResult) this).bib_number, (SingleRaceInternal) this.getRace());
        final int recorded_position2 = getRecordedPosition(((SingleRaceResult) other).bib_number, (SingleRaceInternal) this.getRace());

        if (bib1==58 && bib2==94 || bib1==94 && bib2==58) {
            int x = 3;
        }

        return Integer.compare(recorded_position1, recorded_position2);
    }

    protected int getRecordedPosition(final int bib_number, final SingleRaceInternal race) {

        return (int) race.getRawResults().stream().
            takeWhile(result -> result.getBibNumber() != bib_number).
            count() + 1;
    }
}

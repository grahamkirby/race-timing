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

    // TODO add start time.
    protected Duration finish_time;
    private boolean dnf;
    private final int bib_number;

    public SingleRaceResult(final RaceInternal race, final RaceEntry entry, final Duration finish_time) {

        super(race, entry.participant);

        bib_number = entry.bib_number;
        this.finish_time = finish_time;
    }

    public Performance getPerformance() {
        return new Performance(finish_time);
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

        final Comparator<Performance> comparator = Comparator.nullsLast(Performance::compareTo);
//        final Comparator<RaceResult> comparator2 = new Comparator<RaceResult>() {
//            @Override
//            public int compare(RaceResult o1, RaceResult o2) {
//
//                return comparator.compare(
//                    scorer.getSeriesPerformance((Runner) o1.getParticipant()),
//                    scorer.getSeriesPerformance((Runner) o2.getParticipant()));
//
////                return scorer.compareSeriesPerformance(
////                    scorer.getSeriesPerformance((Runner) o1.getParticipant()),
////                    scorer.getSeriesPerformance((Runner) o2.getParticipant()));
//            }
//        };




        final Performance duration = getPerformance();
        final Performance other_duration = ((SingleRaceResult) other).getPerformance();

        return comparator.compare(duration, other_duration);

//        return duration.compareTo(other_duration);
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

        return (int) ((SingleRaceInternal) race).getRawResults().stream().
            takeWhile(result -> result.getBibNumber() != bib_number).
            count() + 1;
    }
}

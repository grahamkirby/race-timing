/*
 * Copyright 2024 Graham Kirby:
 * <https://github.com/grahamkirby/race-timing>
 *
 * This file is part of the module race-timing.
 *
 * race-timing is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * race-timing is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with race-timing. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.EntryCategory;

import java.time.Duration;

public class IndividualRaceResult extends RaceResult {

    public IndividualRaceEntry entry;
    public boolean DNF;
    public Duration finish_time;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public IndividualRaceResult(final IndividualRace race) {

        super(race);

        // Will be set to false later if a time is processed for this runner.
        DNF = true;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int compareTo(final RaceResult other) {
        return compare(this, other);
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof IndividualRaceResult other && compareTo(other) == 0;
    }

    @Override
    public boolean sameEntrant(final RaceResult other) {
        return entry.equals(((IndividualRaceResult) other).entry);
    }

    @Override
    public boolean completed() {
        return !DNF;
    }

    @Override
    public EntryCategory getCategory() {
        return entry.runner.category;
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {
        return duration().compareTo(((IndividualRaceResult) other).duration());
    }

    public static int comparePerformance(final RaceResult r1, final RaceResult r2) {
        IndividualRaceResult r11 = (IndividualRaceResult) r1;
        Duration duration = r11.duration();
        IndividualRaceResult r21 = (IndividualRaceResult) r2;
        Duration duration1 = r21.duration();

        int i = duration.compareTo(duration1);
        if (r11.entry.runner.name.equals("Isabel Ritchie") && r21.entry.runner.name.equals("Leland Donaldson")) {
            int x = 3;
        }
        return i;
    }

    @Override
    public boolean shouldDisplayPosition() {
        return completed();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public Duration duration() {
        return DNF ? Race.DUMMY_DURATION : finish_time;
    }

    public static int compare(final RaceResult r1, final RaceResult r2) {
//throw new RuntimeException();
        final int compare_completion = r1.compareCompletionTo(r2);
        if (compare_completion != 0) return compare_completion;

        // Now either both have completed or neither have completed.

        final int compare_performance = r1.comparePerformanceTo(r2);
        if (compare_performance != 0) return compare_performance;

        // Now both have the same time. If they have completed, use the recording order,
        // otherwise use alphabetical order for DNFs.

        if (r1.completed())
            return ((IndividualRaceResult)r1).compareRecordedPositionTo((IndividualRaceResult)r2);
        else
            return ((IndividualRaceResult)r1).compareRunnerNameTo((IndividualRaceResult)r2);
    }


    public static int compare2(final RaceResult r1, final RaceResult r2) {

//        Comparator<RaceResult> comparator1 = RaceResult::compareCompletion;
//        Comparator<RaceResult> comparator2 = RaceResult::comparePerformanceTo;
//
//
//        Comparator<RaceResult> comparator6 = IndividualRaceResult::compareRunnerName;

        final int compare_completion = r1.compareCompletionTo(r2);
        if (compare_completion != 0) return compare_completion;

        // Now either both have completed or neither have completed.

        final int compare_performance = r1.comparePerformanceTo(r2);
        if (compare_performance != 0) return compare_performance;

        // Now both have the same time. If they have completed, use the recording order,
        // otherwise use alphabetical order for DNFs.

        if (r1.completed())
            return ((IndividualRaceResult)r1).compareRecordedPositionTo((IndividualRaceResult)r2);
        else
            return ((IndividualRaceResult)r1).compareRunnerNameTo((IndividualRaceResult)r2);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private int compareRecordedPositionTo(final IndividualRaceResult other) {

        final IndividualRace individual_race = (IndividualRace) race;

        final int this_recorded_position = individual_race.getRecordedPosition(entry.bib_number);
        final int other_recorded_position = individual_race.getRecordedPosition(other.entry.bib_number);

        return Integer.compare(this_recorded_position, other_recorded_position);
    }


//    private int compareRunnerName(final RaceResult r1, final RaceResult r2) {
//
//        final int last_name_comparison = race.normalisation.getLastName(entry.runner.name).compareTo(race.normalisation.getLastName(other.entry.runner.name));
//        return last_name_comparison != 0 ? last_name_comparison : race.normalisation.getFirstName(entry.runner.name).compareTo(race.normalisation.getFirstName(other.entry.runner.name));
//    }

    private int compareRunnerNameTo(final IndividualRaceResult other) {

        final int last_name_comparison = race.normalisation.getLastName(entry.runner.name).compareTo(race.normalisation.getLastName(other.entry.runner.name));
        return last_name_comparison != 0 ? last_name_comparison : race.normalisation.getFirstName(entry.runner.name).compareTo(race.normalisation.getFirstName(other.entry.runner.name));
    }
}

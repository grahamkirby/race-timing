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

import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.Category;
import org.grahamkirby.race_timing.common.Race;

import java.time.Duration;

public class IndividualRaceResult extends RaceResult {

    public IndividualRaceEntry entry;
    public boolean DNF;
    public Duration finish_time;

    public IndividualRaceResult(final IndividualRace race) {

        super(race);
        this.DNF = true;
    }

    public Duration duration() {
        return DNF ? Race.DUMMY_DURATION : finish_time;
    }

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
    public Category getCategory() {
        return entry.runner.category;
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {
        return duration().compareTo(((IndividualRaceResult) other).duration());
    }

    private int compareRunnerNameTo(final IndividualRaceResult other) {

        final int last_name_comparison = race.normalisation.getLastName(entry.runner.name).compareTo(race.normalisation.getLastName(other.entry.runner.name));
        return last_name_comparison != 0 ? last_name_comparison : race.normalisation.getFirstName(entry.runner.name).compareTo(race.normalisation.getFirstName(other.entry.runner.name));
    }

    private int compareRecordedPositionTo(final IndividualRaceResult other) {

        final IndividualRace individual_race = (IndividualRace) race;

        final int this_recorded_position = individual_race.getRecordedPosition(entry.bib_number);
        final int other_recorded_position = individual_race.getRecordedPosition(other.entry.bib_number);

        return Integer.compare(this_recorded_position, other_recorded_position);
    }

    public boolean dnf() {
        return DNF;
    }

    public static int compare(final RaceResult r1, final RaceResult r2) {

        final int compare_completion = r1.compareCompletionTo(r2);
        if (compare_completion != 0) return compare_completion;

        // Either both have completed or neither have completed.

        final int compare_performance = r1.comparePerformanceTo(r2);
        if (compare_performance != 0) return compare_performance;

        // Both have the same time. If they have completed, use the recording order,
        // otherwise use alphabetical order for DNFs.

        if (r1.completed())
            return ((IndividualRaceResult)r1).compareRecordedPositionTo((IndividualRaceResult)r2);
        else
            return ((IndividualRaceResult)r1).compareRunnerNameTo((IndividualRaceResult)r2);
    }
}

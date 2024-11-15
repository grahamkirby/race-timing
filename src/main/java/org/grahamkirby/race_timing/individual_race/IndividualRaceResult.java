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

    public IndividualRaceEntry entry;  // Initialised in IndividualRace.fillFinishTimes().
    public boolean DNF;
    public Duration finish_time = Race.DUMMY_DURATION;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public IndividualRaceResult(final IndividualRace race) {

        super(race);

        // Will be set to false later if a time is processed for this runner.
        DNF = true;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

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
    protected String getIndividualRunnerName() {
        return entry == null ? null : entry.runner.name;
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {
        return duration().compareTo(((IndividualRaceResult) other).duration());
    }

    public static int comparePerformance(final RaceResult r1, final RaceResult r2) {

        return ((IndividualRaceResult) r1).duration().compareTo(((IndividualRaceResult) r2).duration());
    }

    @Override
    public boolean shouldDisplayPosition() {
        return completed();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public Duration duration() {
        return finish_time;
    }
}

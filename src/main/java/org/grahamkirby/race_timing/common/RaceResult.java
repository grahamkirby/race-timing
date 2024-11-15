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
package org.grahamkirby.race_timing.common;

import org.grahamkirby.race_timing.common.categories.EntryCategory;

public abstract class RaceResult {

    public final Race race;
    public String position_string;

    protected RaceResult(Race race) {
        this.race = race;
    }

    public static int compareCompletion(final RaceResult r1, final RaceResult r2) {

        if (r1.completed() && !r2.completed()) return -1;
        if (!r1.completed() && r2.completed()) return 1;
        return 0;
    }

    public static int compareRunnerFirstName(final RaceResult r1, final RaceResult r2) {

        String individualRunnerName1 = r1.getIndividualRunnerName();
        String individualRunnerName2 = r2.getIndividualRunnerName();

        if (individualRunnerName1 == null && individualRunnerName2 == null) return 0;
        if (individualRunnerName1 == null) return -1;
        if (individualRunnerName2 == null) return 1;

        return r1.race.normalisation.getFirstName(individualRunnerName1).compareTo(r1.race.normalisation.getFirstName(individualRunnerName2));
    }

    public static int compareRunnerLastName(final RaceResult r1, final RaceResult r2) {

        String individualRunnerName1 = r1.getIndividualRunnerName();
        String individualRunnerName2 = r2.getIndividualRunnerName();

        if (individualRunnerName1 == null && individualRunnerName2 == null) return 0;
        if (individualRunnerName1 == null) return -1;
        if (individualRunnerName2 == null) return 1;

        return r1.race.normalisation.getLastName(r1.getIndividualRunnerName()).compareTo(r1.race.normalisation.getLastName(r2.getIndividualRunnerName()));
    }

    protected abstract String getIndividualRunnerName();
    public abstract int comparePerformanceTo(final RaceResult other);
    public abstract boolean sameEntrant(final RaceResult other);
    public abstract boolean completed();
    public abstract boolean shouldDisplayPosition();
    public abstract EntryCategory getCategory();
}

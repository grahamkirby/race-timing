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

import org.grahamkirby.race_timing.common.categories.Category;

public abstract class RaceResult implements Comparable<RaceResult> {

    public final Race race;
    public String position_string;

    protected RaceResult(Race race) {
        this.race = race;
    }

    public int compareCompletionTo(final RaceResult o) {

        if (completed() && !o.completed()) return -1;
        if (!completed() && o.completed()) return 1;
        return 0;
    }

    public int comparePerformanceTo(final RaceResult other) {
        // Neutral implementation to be overridden.
        return 0;
    }

    public abstract boolean sameEntrant(final RaceResult other);
    public abstract boolean completed();
    public abstract Category getCategory();
}

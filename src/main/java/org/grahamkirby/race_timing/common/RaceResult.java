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
import org.grahamkirby.race_timing.common.categories.PrizeCategory;

public abstract class RaceResult {

    public final Race race;
    public String position_string;
    public PrizeCategory category_of_prize_awarded = null;
    public CompletionStatus completion_status;

    protected RaceResult(Race race) {
        this.race = race;
    }

    protected abstract String getIndividualRunnerName();
    public abstract int comparePerformanceTo(final RaceResult other);
    public abstract boolean completed();
    public abstract boolean shouldDisplayPosition();
    public abstract EntryCategory getCategory();
}

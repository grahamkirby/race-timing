/*
 * Copyright 2025 Graham Kirby:
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

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("IncorrectFormatting")
public abstract class RaceResult {

    public final Race race;
    public String position_string;
    List<PrizeCategory> categories_of_prizes_awarded = new ArrayList<>();

    protected RaceResult(final Race race) {
        this.race = race;
    }

    // TODO push down to individual races.
    protected String getIndividualRunnerClub() {
        return null;
    }

    protected abstract String getIndividualRunnerName();

    public abstract Participant getParticipant();
    public abstract int comparePerformanceTo(RaceResult other);
    public abstract boolean canComplete();
    public abstract boolean shouldDisplayPosition();
    public abstract EntryCategory getCategory();
}

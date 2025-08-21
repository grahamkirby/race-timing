/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (graham.kirby@st-andrews.ac.uk)
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
package org.grahamkirby.race_timing_experimental.common;


import org.grahamkirby.race_timing.common.Participant;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("IncorrectFormatting")
public abstract class RaceResult {

    public final Race race;
    public String position_string;
    public List<PrizeCategory> categories_of_prizes_awarded = new ArrayList<>();

    protected RaceResult(final Race race) {
        this.race = race;
    }

    public abstract String getParticipantName();
    public abstract Participant getParticipant();
    public abstract int comparePerformanceTo(RaceResult other);
    public abstract boolean canComplete();
    public abstract boolean shouldDisplayPosition();
    public abstract EntryCategory getCategory();
}

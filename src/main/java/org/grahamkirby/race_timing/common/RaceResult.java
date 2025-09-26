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


import org.grahamkirby.race_timing.categories.EntryCategory;
import org.grahamkirby.race_timing.categories.PrizeCategory;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("IncorrectFormatting")
public abstract class RaceResult {

    protected final Race race;
    protected Participant participant;
    protected String position_string;
    protected List<PrizeCategory> categories_of_prizes_awarded = new ArrayList<>();

    protected RaceResult(final Race race, final Participant participant) {
        this.race = race;
        this.participant = participant;
    }

    public Race getRace() {
        return race;
    }

    public Participant getParticipant() {
        return participant;
    }

    public String getParticipantName() {
        return participant.name;
    }

    public EntryCategory getCategory() {
        return participant.category;
    }

    public String getPositionString() {
        return position_string;
    }

    public void setPositionString(final String position_string) {
        this.position_string  = position_string;
    }

    public List<PrizeCategory> getCategoriesOfPrizesAwarded() {
        return categories_of_prizes_awarded;
    }

    public abstract int comparePerformanceTo(RaceResult other);
    public abstract boolean canComplete();
    public abstract boolean shouldDisplayPosition();
}

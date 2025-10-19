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
package org.grahamkirby.race_timing.individual_race;


import org.grahamkirby.race_timing.categories.EntryCategory;
import org.grahamkirby.race_timing.common.Participant;

import java.util.Objects;

public class Runner extends Participant {

    private String club;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public Runner(final String name, final String club, final EntryCategory category) {

        super(name, category);
        this.club = club;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public String getClub() {
        return club;
    }
    public void setClub(final String club) {
        this.club = club;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Override
    public boolean equals(final Object obj) {

        return obj instanceof final Runner other_runner &&
            getName().equals(other_runner.getName()) &&
            club.equals(other_runner.club);
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        return Objects.hash(getName(), club);
    }

    @Override
    public String toString() {
        return getName() + ", " + club;
    }
}

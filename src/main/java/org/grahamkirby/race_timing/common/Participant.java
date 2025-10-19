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

public abstract class Participant {

    protected final String name;
    protected EntryCategory category; // Not final since may be updated if category changes during a series.

    protected Participant(final String name, final EntryCategory category) {
        this.name = name;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public EntryCategory getCategory() {
        return category;
    }

    public void setCategory(final EntryCategory category) {
        this.category = category;
    }
}

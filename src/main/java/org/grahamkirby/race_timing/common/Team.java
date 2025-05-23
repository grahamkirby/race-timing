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

import java.util.List;
import java.util.Objects;

public class Team extends Participant {

    public List<String> runner_names;

    public Team(final String name, final EntryCategory category, final List<String> runner_names) {
        super(name, category);
        this.runner_names = runner_names;
    }

    @Override
    public boolean equals(final Object obj) {

        return obj instanceof final Team other_team &&
            name.equals(other_team.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}

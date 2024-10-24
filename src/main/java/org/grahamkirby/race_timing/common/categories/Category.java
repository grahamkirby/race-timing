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
package org.grahamkirby.race_timing.common.categories;

public abstract class Category {

    private final String long_name;
    private final String short_name;
    private final String gender;

    private final int minimum_age;
    private final int maximum_age;

    public Category(final String long_name, final String short_name, final String gender, final int minimum_age, final int maximum_age) {

        this.long_name = long_name;
        this.short_name = short_name;
        this.gender = gender;
        this.minimum_age = minimum_age;
        this.maximum_age = minimum_age;
    }

    public String getLongName() {
        return long_name;
    }

    public String getShortName() {
        return short_name;
    }

    public String getGender() {
        return gender;
    }

    public int getMinimumAge() {
        return minimum_age;
    }


    @Override
    public boolean equals(final Object obj) {
        // TODO check on gender and age bracket
        return obj instanceof Category other && short_name.equals(other.short_name);
    }

    @Override
    public int hashCode() {
        return short_name.hashCode();
    }
}

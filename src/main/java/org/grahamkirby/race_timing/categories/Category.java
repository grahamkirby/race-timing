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
package org.grahamkirby.race_timing.categories;


import java.util.Objects;

/**
 * Parent class for entry category and prize category,
 * each including a gender and age range.
 */
public abstract class Category {

    // E.g. "Men Senior", "Men 40-49".
    private final String long_name;

    // E.g. "MS", "M40".
    private final String short_name;

    // e.g "Women", "Female", "Mixed", "Open".
    private final String gender;

    // Both ages are inclusive.
    private final int minimum_age;
    private final int maximum_age;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    Category(final String components) {

        final String[] parts = components.split(",");

        long_name = parts[0];
        short_name = parts[1];
        gender = parts[2];
        minimum_age = Integer.parseInt(parts[3]);
        maximum_age = Integer.parseInt(parts[4]);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

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

    public int getMaximumAge() {
        return maximum_age;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Equality defined in terms of gender and age range.
     */
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof final Category other && gender.equals(other.gender) && minimum_age == other.minimum_age && maximum_age == other.maximum_age;
    }

    /**
     * Hash code defined in terms of gender and age range.
     */
    @Override
    public int hashCode() {
        return Objects.hash(gender, minimum_age, maximum_age);
    }
}

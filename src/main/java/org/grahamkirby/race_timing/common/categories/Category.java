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

import java.util.Objects;

/**
 * Parent class for entry category and prize category,
 * each including a gender and age range.
 */
public abstract class Category {

    private record CategoryComponents(String long_name, String short_name, String gender,
                                      int minimum_age, int maximum_age) {
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

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

        this(getCategoryComponents(components));
    }

    private Category(final CategoryComponents components) {

        long_name = components.long_name();
        short_name = components.short_name();
        gender = components.gender();
        minimum_age = components.minimum_age();
        maximum_age = components.maximum_age();
    }

    private static CategoryComponents getCategoryComponents(final String components) {

        final String[] parts = components.split(",");

        final String long_name = parts[0];
        final String short_name = parts[1];
        final String gender = parts[2];
        final int minimum_age = Integer.parseInt(parts[3]);
        final int maximum_age = Integer.parseInt(parts[4]);

        return new CategoryComponents(long_name, short_name, gender, minimum_age, maximum_age);
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

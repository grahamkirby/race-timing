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

public class AgeRange {

    private final int minimum_age;
    private final int maximum_age;

    AgeRange(final int minimum_age, final int maximum_age) {

        this.minimum_age = minimum_age;
        this.maximum_age = maximum_age;
    }

    public int getMinimumAge() {
        return minimum_age;
    }
    public int getMaximumAge() {
        return maximum_age;
    }

    public boolean intersectsWith(AgeRange other) {

        if (this == other) return false;



        int min1 = minimum_age;
        int min2 = other.minimum_age;
        int max1 = maximum_age;
        int max2 = other.maximum_age;

        return min1 < min2 && max1 >= min2 && max1 < max2 ||
            min1 == min2 && max1 == max2;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof final AgeRange other && minimum_age == other.minimum_age && maximum_age == other.maximum_age;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minimum_age, maximum_age);
    }
}

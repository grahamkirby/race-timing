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
 * Category specific to a particular runner or team entry.
 * <br />
 * Values are read from a configuration file such as
 * {@link /src/main/resources/configuration/categories_entry_individual_junior.csv}.
 */
public final class EntryCategory extends Category {

    // e.g "Women", "Female", "Mixed", "Non-Binary".
    private final String gender;

    /**
     * Creates an instance from a comma-separated string containing:
     * long name, short name, gender, minimum age, maximum age.
     * Minimum and maximum ages are inclusive.
     */
    public EntryCategory(final String components) {

        super(components);

        final String[] parts = components.split(",");

        gender = parts[2];
    }

    public String getGender() {
        return gender;
    }

    /**
     * Equality defined in terms of gender and age range.
     */
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof final EntryCategory other && gender.equals(other.gender) && age_range.equals(other.age_range);
    }

    /**
     * Hash code defined in terms of gender and age range.
     */
    @Override
    public int hashCode() {
        return Objects.hash(gender, age_range);
    }
}

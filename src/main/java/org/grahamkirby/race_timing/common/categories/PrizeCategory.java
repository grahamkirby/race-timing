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
package org.grahamkirby.race_timing.common.categories;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Category defining eligibility for a particular prize.
 * This is different from entry category, since multiple entry categories
 * may be eligible for a given prize category e.g. an open prize category
 * may include multiple age categories.
 * <br />
 * Values are read from a configuration file such as
 * {@link /src/main/resources/configuration/categories_prize_individual_junior.csv}.
 */
public final class PrizeCategory extends Category {

    private final int number_of_prizes;
    private final Set<String> eligible_clubs;
    private final boolean exclusive;

    /**
     * Creates an instance from a comma-separated string containing:
     * long name, short name, gender, minimum age, maximum age, number of prizes.
     * Minimum and maximum ages are inclusive.
     */
    public PrizeCategory(final String components) {

        super(components);
        final String[] elements = components.split(",");
        number_of_prizes = Integer.parseInt(elements[5]);

        eligible_clubs = new HashSet<>();
        if (elements.length >= 8) {
            final String club_string = elements[7];
            if (!club_string.isEmpty())
                eligible_clubs.addAll(Arrays.stream(club_string.split("/")).toList());
        }

        exclusive = elements.length < 9 || elements[8].equals("Y");
    }

    public int numberOfPrizes() {
        return number_of_prizes;
    }

    public Set<String> getEligibleClubs() {
        return eligible_clubs;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    /**
     * Equality defined in terms of gender and age range.
     */
    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj) && obj instanceof final PrizeCategory other && number_of_prizes == other.number_of_prizes && eligible_clubs.equals(other.eligible_clubs) && exclusive == other.exclusive;
    }

    /**
     * Hash code defined in terms of gender and age range.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), number_of_prizes, eligible_clubs, exclusive);
    }
}

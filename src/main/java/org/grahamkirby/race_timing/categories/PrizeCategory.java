/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2026 Graham Kirby (race-timing@kirby-family.net)
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


import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;

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

    private final Set<String> eligible_genders;
    private final int number_of_prizes;
    private final String group;
    private final Set<String> eligible_clubs;
    private final boolean exclusive;

    /**
     * Creates an instance from a comma-separated string containing:
     * long name, short name, gender, minimum age (inclusive), maximum age (inclusive), number of prizes,
     * category group, eligible clubs, whether exclusive.
     */
    public PrizeCategory(final String components) {

        // Category definition headers (last two optional):
        //
        // Long Category Name, Short Category Name, Eligible Gender(s), Minimum Age, Maximum Age, Number of Prizes, Category Group, [Eligible Clubs, default all], [Exclusive (Y/N), default Y]

        super(components);

        final String[] elements = components.split(",");

        eligible_genders = getGenders(elements);
        number_of_prizes = Integer.parseInt(elements[PRIZES_INDEX]);
        group = elements[GROUP_INDEX];
        eligible_clubs = getEligibleClubs(elements);
        exclusive = getExclusive(elements);
    }

    private Set<String> getGenders(final String[] elements) {

        final String[] split = elements[GENDER_INDEX].split("/");
        return Arrays.stream(split).map(String::trim).collect(Collectors.toSet());
    }

    private Set<String> getEligibleClubs(final String[] elements) {

        if (elements.length >= CLUBS_INDEX + 1) {
            final String club_string = elements[CLUBS_INDEX];
            if (!club_string.isEmpty())
                return Arrays.stream(club_string.split("/")).collect(Collectors.toSet());
        }

        return new HashSet<>();
    }

    private boolean getExclusive(final String[] elements) {

        // Default is TRUE if not defined.
        return elements.length <= EXCLUSIVE_INDEX || elements[EXCLUSIVE_INDEX].equals("Y");
    }

    public int numberOfPrizes() {
        return number_of_prizes;
    }

    public String getGroup() {
        return group;
    }

    public Set<String> getEligibleClubs() {
        return eligible_clubs;
    }

    public Set<String> getEligibleGenders() {
        return eligible_genders;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    @Override
    public boolean intersectsWith(final Category o) {

        return o instanceof final PrizeCategory other && super.intersectsWith(other) && exclusive && other.exclusive;
    }

    @Override
    public boolean equals(final Object obj) {

        return obj instanceof final PrizeCategory other &&
            number_of_prizes == other.number_of_prizes &&
            group.equals(other.group) &&
            eligible_clubs.equals(other.eligible_clubs) &&
            eligible_genders.equals(other.eligible_genders) &&
            exclusive == other.exclusive &&
            age_range.equals(other.age_range);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number_of_prizes, group, eligible_clubs, eligible_genders, exclusive, age_range);
    }

    // Gender eligibility ordering based only on the number of eligible genders.
    private int compareByIncreasingGenderCategoryGenerality(final PrizeCategory other) {

        final Comparator<PrizeCategory> comparator = comparingInt(category -> category.getEligibleGenders().size());

        return comparator.compare(this, other);
    }

    private int compareByDecreasingGenderCategoryGenerality(final PrizeCategory other) {

        return -compareByIncreasingGenderCategoryGenerality(other);
    }

    private int compareByDecreasingAgeCategoryGenerality(final PrizeCategory other) {

        return getAgeRange().compareByDecreasingGenerality(other.getAgeRange());
    }

    public int compareTo(final PrizeCategory other) {

        final int age_comparison = compareByDecreasingAgeCategoryGenerality(other);
        final int gender_comparison = compareByDecreasingGenderCategoryGenerality(other);

        return age_comparison != 0 ? age_comparison : gender_comparison;
    }
}

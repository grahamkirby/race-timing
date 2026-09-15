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


import org.grahamkirby.race_timing.common.NormalisationProcessor;

import static org.grahamkirby.race_timing.common.Config.ERROR_AGE_RANGE_INVALID;
import static org.grahamkirby.race_timing.common.Config.ERROR_CATEGORY_MISSING_ELEMENTS;

/**
 * Parent class for entry category and prize category.
 */
public abstract class Category {

    // Entry category definition file headers:
    //
    // Long Category Name, Short Category Name, Gender, Minimum Age, Maximum Age
    //
    // Prize category definition file headers (last two optional):
    //
    // Long Category Name, Short Category Name, Eligible Gender(s), Minimum Age, Maximum Age, Number of Prizes, Category Group, [Eligible Clubs], [Exclusive (Y/N)]

    protected static final int CATEGORY_INDEX_LONG_NAME = 0;
    protected static final int CATEGORY_INDEX_SHORT_NAME = 1;
    protected static final int CATEGORY_INDEX_GENDER = 2;
    protected static final int CATEGORY_INDEX_MINIMUM_AGE = 3;
    protected static final int CATEGORY_INDEX_MAXIMUM_AGE = 4;
    protected static final int CATEGORY_INDEX_PRIZES = 5;
    protected static final int CATEGORY_INDEX_GROUP = 6;
    protected static final int CATEGORY_INDEX_CLUBS = 7;
    protected static final int CATEGORY_INDEX_EXCLUSIVE = 8;

    // E.g. "Men Senior", "Men 40-49".
    private final String long_name;

    // E.g. "MS", "M40".
    private final String short_name;

    // Both ages are inclusive.
    protected final AgeRange age_range;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    Category(final String components) {

        final String[] parts = components.split(",", -1);

        if (parts.length <= CATEGORY_INDEX_MAXIMUM_AGE)
            throw new RuntimeException(ERROR_CATEGORY_MISSING_ELEMENTS);

        long_name = NormalisationProcessor.cleanSpacesAndQuotes(parts[CATEGORY_INDEX_LONG_NAME]);
        short_name = NormalisationProcessor.cleanSpacesAndQuotes(parts[CATEGORY_INDEX_SHORT_NAME]);

        final String minimum_age_as_string = parts[CATEGORY_INDEX_MINIMUM_AGE];
        final String maximum_age_as_string = parts[CATEGORY_INDEX_MAXIMUM_AGE];

        try {
            age_range = new AgeRange(Integer.parseInt(minimum_age_as_string), Integer.parseInt(maximum_age_as_string));
        }
        catch (NumberFormatException _) {
            throw new RuntimeException(ERROR_AGE_RANGE_INVALID + " for category: " + minimum_age_as_string + ", " + maximum_age_as_string);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean intersectsWith(final Category other) {

        return age_range.intersectsWith(other.age_range);
    }

    public String getLongName() {
        return long_name;
    }

    public String getShortName() {
        return short_name;
    }

    public AgeRange getAgeRange() {
        return age_range;
    }

    @Override
    public String toString() {

        return getShortName() + " (" + getAgeRange().getMinimumAge() + "," + getAgeRange().getMaximumAge() + ")";
    }
}

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

/**
 * Category defining eligibility for a particular prize.
 * This is separate from entry category since multiple entry categories
 * may be eligible for a prize category e.g. an open prize category
 * may include multiple age categories.
 */
public class PrizeCategory extends Category {

    public static final int PRIZE_CATEGORY_GROUP_NAME_INDEX = 6;

    private final int number_of_prizes;

    public PrizeCategory(final String long_name, final String short_name, final String gender, final int minimum_age, final int maximum_age, final int number_of_prizes) {

        super(long_name, short_name, gender, minimum_age, maximum_age);
        this.number_of_prizes = number_of_prizes;
    }

    public static PrizeCategory makePrizeCategory(final String line) {

        final String[] parts = line.split(",");

        final String long_name = parts[0];
        final String short_name = parts[1];
        final String gender = parts[2];
        final int minimum_age = Integer.parseInt(parts[3]);
        final int maximum_age = Integer.parseInt(parts[4]);
        final int number_of_prizes = Integer.parseInt(parts[5]);

        return new PrizeCategory(long_name, short_name, gender, minimum_age, maximum_age, number_of_prizes);
    }

    public int numberOfPrizes() {
        return number_of_prizes;
    }
}

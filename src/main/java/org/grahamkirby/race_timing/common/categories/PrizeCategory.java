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
 * This is different from entry category, since multiple entry categories
 * may be eligible for a given prize category e.g. an open prize category
 * may include multiple age categories.
 */
public final class PrizeCategory extends Category {

    private final int number_of_prizes;

    /**
     * Creates an instance from a comma-separated string containing:
     * long name, short name, gender, minimum age, maximum age, number of prizes.
     * Minimum and maximum ages are inclusive.
     */
    public PrizeCategory(final String components) {

        super(components);
        number_of_prizes = Integer.parseInt(components.split(",")[5]);
    }

    public int numberOfPrizes() {
        return number_of_prizes;
    }
}

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

public class PrizeCategory extends Category {

    private int number_of_prizes;

    public PrizeCategory(final String long_name, final String short_name, final String gender, final int minimum_age, final int maximum_age, final int number_of_prizes) {

        super(long_name, short_name, gender, minimum_age, maximum_age);
        this.number_of_prizes = number_of_prizes;
    }

    public PrizeCategory(String line) {
        this(null, null, line, 0, 0, 0);
    }

    public int numberOfPrizes() {
        return number_of_prizes;
    }
}

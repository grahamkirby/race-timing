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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PrizeCategoryTest {

    @Test
    public void comparisonTest() {

        // Long Category Name, Short Category Name, Eligible Gender(s), Minimum Age, Maximum Age, Number of Prizes, Category Group, [Eligible Clubs, default all], [Exclusive (Y/N), default Y]

        final PrizeCategory category1 = new PrizeCategory(",,Male,20,40,1,General");
        final PrizeCategory category2 = new PrizeCategory(",,Male,25,30,1,General");
        final PrizeCategory category3 = new PrizeCategory(",,Female,20,40,1,General");
        final PrizeCategory category4 = new PrizeCategory(",,Female/Male,20,40,1,General");
        final PrizeCategory category5 = new PrizeCategory(",,Female/Male,25,30,1,General");

        assertEquals(0, category1.compareTo(category1));
        assertEquals(0, category5.compareTo(category5));

        assertEquals(-1, category1.compareTo(category2));
        assertEquals(1, category2.compareTo(category1));

        assertEquals(-1, category4.compareTo(category1));
        assertEquals(1, category1.compareTo(category4));

        assertEquals(-1, category1.compareTo(category5));
        assertEquals(1, category5.compareTo(category1));

        assertEquals(0, category1.compareTo(category3));
        assertEquals(0, category3.compareTo(category1));
    }
}

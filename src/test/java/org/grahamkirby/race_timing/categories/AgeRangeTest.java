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

public class AgeRangeTest {

    @Test
    public void rangeIsValid() {

        new AgeRange(10, 20);
        new AgeRange(20, 20);
        new AgeRange(20, 21);

        assertThrows(
            RuntimeException.class,
            () -> new AgeRange(20, 19)
        );
    }

    @Test
    public void disjointTest() {

        assertTrue(new AgeRange(10, 20).disjoint(new AgeRange(21, 30)));
        assertTrue(new AgeRange(21, 30).disjoint(new AgeRange(10, 20)));

        assertFalse(new AgeRange(20, 30).disjoint(new AgeRange(10, 20)));
        assertFalse(new AgeRange(10, 20).disjoint(new AgeRange(20, 30)));
        assertFalse(new AgeRange(20, 30).disjoint(new AgeRange(20, 30)));
        assertFalse(new AgeRange(20, 30).disjoint(new AgeRange(22, 30)));
        assertFalse(new AgeRange(20, 30).disjoint(new AgeRange(22, 28)));
    }

    @Test
    public void intersectsTest() {

        assertFalse(new AgeRange(10, 20).intersectsWith(new AgeRange(21, 30)));
        assertFalse(new AgeRange(21, 30).intersectsWith(new AgeRange(10, 20)));

        assertTrue(new AgeRange(10, 20).intersectsWith(new AgeRange(20, 30)));
        assertTrue(new AgeRange(20, 30).intersectsWith(new AgeRange(20, 30)));
        assertFalse(new AgeRange(20, 30).intersectsWith(new AgeRange(22, 30)));
        assertFalse(new AgeRange(20, 30).intersectsWith(new AgeRange(22, 28)));
    }

    @Test
    public void containsTest() {

        assertFalse(new AgeRange(10, 20).contains(new AgeRange(21, 30)));
        assertFalse(new AgeRange(21, 30).contains(new AgeRange(10, 20)));

        assertFalse(new AgeRange(10, 20).contains(new AgeRange(20, 30)));
        assertTrue(new AgeRange(20, 30).contains(new AgeRange(20, 30)));
        assertTrue(new AgeRange(20, 30).contains(new AgeRange(22, 30)));
        assertTrue(new AgeRange(20, 30).contains(new AgeRange(22, 28)));
        assertFalse(new AgeRange(22, 28).contains(new AgeRange(20, 30)));
    }

    @Test
    public void generalityTest() {

        assertEquals(0, new AgeRange(10, 20).compareByDecreasingGenerality(new AgeRange(21, 30)));
        assertEquals(0, new AgeRange(21, 30).compareByDecreasingGenerality(new AgeRange(10, 20)));
        assertEquals(0, new AgeRange(10, 20).compareByDecreasingGenerality(new AgeRange(10, 20)));

        assertEquals(-1, new AgeRange(20, 30).compareByDecreasingGenerality(new AgeRange(22, 30)));
        assertEquals(-1, new AgeRange(20, 30).compareByDecreasingGenerality(new AgeRange(22, 28)));
        assertEquals(1, new AgeRange(22, 28).compareByDecreasingGenerality(new AgeRange(20, 30)));
    }
}

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

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EqualsTest {

    @Test
    public void verifyEqualsAgeRange() {

        EqualsVerifier.
            forClass(AgeRange.class).
            suppress(Warning.INHERITED_DIRECTLY_FROM_OBJECT).
            verify();
    }

    @Test
    public void verifyEqualsEntryCategory() {

        EntryCategory red = new EntryCategory("Category 1,Cat1,Male,18,39");
        EntryCategory blue = new EntryCategory("Category 2,Cat2,Male/Female,18,99");

        EqualsVerifier.
            forExamples(red, blue).
            withIgnoredFields("long_name", "short_name").
            verify();
    }

    @Test
    public void verifyEqualsPrizeCategory() {

        PrizeCategory red = new PrizeCategory("Category 1,Cat1,Male,18,39,1,Overall,Fife AC,Y");
        PrizeCategory blue = new PrizeCategory("Category 2,Cat2,Male/Female,18,99,3,Mixed,,N");

        EqualsVerifier.
            forExamples(red, blue).
            withIgnoredFields("long_name", "short_name").
            verify();
    }
}

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
package org.grahamkirby.race_timing.relay_race;


import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RacePrizes;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class RelayRacePrizes extends RacePrizes {

    RelayRacePrizes(final Race race) {

        super(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final List<String> GENDER_ORDER = Arrays.asList("Open", "Women", "Mixed");

    @Override
    public void allocatePrizes() {

        // Allocate first prize in each category first, in decreasing order of category breadth.
        // This is because e.g. a 40+ team should win first in 40+ category before a subsidiary
        // prize in open category.

        final List<PrizeCategory> categories_sorted_by_decreasing_generality = sortByDecreasingGenerality(race.getPrizeCategories());

        allocateFirstPrizes(categories_sorted_by_decreasing_generality);
        allocateMinorPrizes(categories_sorted_by_decreasing_generality);
    }

    private static List<PrizeCategory> sortByDecreasingGenerality(final List<PrizeCategory> prize_categories) {

        final List<PrizeCategory> sorted_categories = new ArrayList<>(prize_categories);

        sorted_categories.sort(Comparator.comparingInt((PrizeCategory category) -> category.getMinimumAge()).thenComparingInt(category -> GENDER_ORDER.indexOf(category.getGender())));

        return sorted_categories;
    }

    private void allocateFirstPrizes(final Iterable<PrizeCategory> prize_categories) {

        for (final PrizeCategory category : prize_categories)
            for (final RaceResult result : race.getOverallResults())
                if (isPrizeWinner(result, category)) {
                    setPrizeWinner(result, category);
                    break;
                }
    }

    private void allocateMinorPrizes(final Iterable<PrizeCategory> prize_categories) {

        for (final PrizeCategory category : prize_categories)
            allocateMinorPrizes(category);
    }

    private void allocateMinorPrizes(final PrizeCategory category) {

        int position = 2;

        for (final RaceResult result : race.getOverallResults()) {

            if (position > category.numberOfPrizes()) return;

            if (isPrizeWinner(result, category)) {
                setPrizeWinner(result, category);
                position++;
            }
        }
    }
}

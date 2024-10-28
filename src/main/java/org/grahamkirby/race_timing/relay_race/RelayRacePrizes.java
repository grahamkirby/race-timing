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
package org.grahamkirby.race_timing.relay_race;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RacePrizes;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RelayRacePrizes extends RacePrizes {

    public RelayRacePrizes(final Race race) {

        super(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final List<String> GENDER_ORDER = Arrays.asList("Open", "Women", "Mixed");

    @Override
    public void allocatePrizes() {

        for (final PrizeCategory category : race.getPrizeCategories())
            race.prize_winners.put(category, new ArrayList<>());

        // Allocate first prize in each category first, in decreasing order of category breadth.
        // This is because e.g. a 40+ team should win first in 40+ category before a subsidiary
        // prize in open category.

        final List<PrizeCategory> categories_sorted_by_decreasing_generality = sortByDecreasingGenerality(race.getPrizeCategories());

        allocateFirstPrizes(categories_sorted_by_decreasing_generality);

        // Now consider other prizes.
        allocateMinorPrizes(categories_sorted_by_decreasing_generality);
    }

    public static List<PrizeCategory> sortByDecreasingGenerality(final List<PrizeCategory> prize_categories) {

        final List<PrizeCategory> result = new ArrayList<>(prize_categories);
        result.sort((category1, category2) -> {

            if (category1.equals(category2)) return 0;

            final int compare_minimum_age = Integer.compare(category1.getMinimumAge(), category2.getMinimumAge());
            if (compare_minimum_age != 0) return compare_minimum_age;

            return Integer.compare(GENDER_ORDER.indexOf(category1.getGender()), GENDER_ORDER.indexOf(category2.getGender()));
        });
        return result;
    }

    private void allocateFirstPrizes(final List<PrizeCategory> prize_categories) {

        for (final PrizeCategory category : prize_categories) {

            for (final RaceResult result : race.getOverallResults()) {

                if (prizeWinner(result, category)) {
                    race.prize_winners.get(category).add(result);
                    break;
                }
            }
        }
    }

    private void allocateMinorPrizes(final List<PrizeCategory> prize_categories) {

        for (final PrizeCategory category : prize_categories)
            allocateMinorPrizes(category);
    }

    private void allocateMinorPrizes(final PrizeCategory category) {

        int position = 2;

        for (final RaceResult result : race.getOverallResults()) {

            if (position > category.numberOfPrizes()) return;

            if (prizeWinner(result, category)) {
                race.prize_winners.get(category).add(result);
                position++;
            }
        }
    }
}

/*
 * Copyright 2025 Graham Kirby:
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
package org.grahamkirby.race_timing.common;

import org.grahamkirby.race_timing.common.categories.PrizeCategory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RacePrizes {

    protected final Race race;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public RacePrizes(final Race race) {
        this.race = race;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public void allocatePrizes() {

        for (final PrizeCategory category : race.getPrizeCategories())
            setPrizeWinners(category);
    }

    protected boolean isPrizeWinner(final RaceResult result, final PrizeCategory prize_category) {

        return hasNotAlreadyWonPrize(result) &&
            result.getCompletionStatus() == CompletionStatus.COMPLETED &&
            race.isEntryCategoryEligibleForPrizeCategory(result.getCategory(), prize_category);
    }

    private static boolean hasNotAlreadyWonPrize(final RaceResult result) {

        return result.category_of_prize_awarded == null;
    }

    protected static void setPrizeWinner(final RaceResult result, final PrizeCategory category) {

        result.category_of_prize_awarded = category;
    }

    public List<RaceResult> getPrizeWinners(final PrizeCategory prize_category) {

        final List<RaceResult> prize_results = race.getOverallResults().stream().
            filter(result -> prize_category.equals(result.category_of_prize_awarded)).
            toList();

        race.setPositionStrings(prize_results);

        return prize_results;
    }

    private void setPrizeWinners(final PrizeCategory category) {

        final AtomicInteger position = new AtomicInteger(1);

        race.getOverallResults().stream().
            filter(_ -> position.get() <= category.numberOfPrizes()).
            filter(result -> isPrizeWinner(result, category)).
            forEachOrdered(result -> {
                position.getAndIncrement();
                setPrizeWinner(result, category);
            });
    }

    public boolean arePrizesInThisOrLaterCategory(final PrizeCategory category) {

        for (final PrizeCategory category2 : race.getPrizeCategories().reversed()) {

            if (!race.prizes.getPrizeWinners(category2).isEmpty()) return true;
            if (category.equals(category2) && !arePrizesInOtherCategorySameAge(category)) return false;
        }
        return false;
    }

    private boolean arePrizesInOtherCategorySameAge(final PrizeCategory category) {

        return race.getPrizeCategories().stream().
            filter(cat -> !cat.equals(category)).
            filter(cat -> cat.getMinimumAge() == category.getMinimumAge()).
            anyMatch(cat -> !race.prizes.getPrizeWinners(cat).isEmpty());
    }
}

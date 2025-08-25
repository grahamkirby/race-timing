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
package org.grahamkirby.race_timing.common;


import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;

import java.util.*;

@SuppressWarnings("IncorrectFormatting")
public abstract class RaceResult {

    public final Race race;
    public String position_string;
    List<PrizeCategory> categories_of_prizes_awarded = new ArrayList<>();

    protected RaceResult(final Race race) {
        this.race = race;
    }

    protected abstract String getParticipantName();

    public abstract Participant getParticipant();
    public abstract int comparePerformanceTo(RaceResult other);
    public abstract boolean canComplete();
    public abstract boolean shouldDisplayPosition();
    public abstract EntryCategory getCategory();

    /** Tests whether the given entry category is eligible for the given prize category. */
    public boolean isResultEligibleForPrizeCategory(final PrizeCategory prize_category) {

        return isResultEligibleForPrizeCategoryByGender( prize_category) &&
            isResultEligibleForPrizeCategoryByAge( prize_category);
    }

    /** Tests whether the given entry category is eligible in any of the given prize categories. */
    public boolean isResultEligibleInSomePrizeCategory(final Collection<PrizeCategory> prize_categories) {

        return prize_categories.stream().
            anyMatch(this::isResultEligibleForPrizeCategory);
    }

    private boolean isResultEligibleForPrizeCategoryByGender(final PrizeCategory prize_category) {

        // It's possible for the entry category to be null in a series race, where some of the individual
        // race results may not include entry categories.
        final EntryCategory entry_category = getCategory();

        if (entry_category == null) return false;

        final Map<String, List<String>> gender_eligibility_map = race.normalisation.gender_eligibility_map;

        return gender_eligibility_map.keySet().stream().
            filter(entry_gender -> entry_category.getGender().equals(entry_gender)).
            anyMatch(entry_gender -> gender_eligibility_map.get(entry_gender).contains(prize_category.getGender()));
    }

    private boolean isResultEligibleForPrizeCategoryByAge(final PrizeCategory prize_category) {

        // It's possible for the entry category to be null in a series race, where some of the individual
        // race results may not include entry categories.
        final EntryCategory entry_category = getCategory();

        return entry_category != null &&
            entry_category.getMinimumAge() >= prize_category.getMinimumAge() &&
            entry_category.getMaximumAge() <= prize_category.getMaximumAge();
    }
}

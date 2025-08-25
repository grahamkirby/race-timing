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
package org.grahamkirby.race_timing_experimental.common;

import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategoryGroup;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CategoryDetailsImpl implements CategoryDetails {

    private final List<EntryCategory> entry_categories;
    private final List<PrizeCategoryGroup> prize_category_groups;

    public CategoryDetailsImpl(List<EntryCategory> entry_categories, List<PrizeCategoryGroup> prize_category_groups) {

        this.entry_categories = entry_categories;
        this.prize_category_groups = prize_category_groups;
    }

    @Override
    public List<PrizeCategory> getPrizeCategories() {

        return prize_category_groups.stream().
            flatMap(group -> group.categories().stream()).
            toList();
    }

    @Override
    public List<PrizeCategoryGroup> getPrizeCategoryGroups() {
        return prize_category_groups;
    }

    @Override
    public EntryCategory lookupEntryCategory(final String short_name) {

        return entry_categories.stream().
            filter(category -> category.getShortName().equals(short_name)).
            findFirst().
            orElseThrow();
    }

    /** Tests whether the given entry category is eligible for the given prize category. */
    @Override
    public boolean isResultEligibleForPrizeCategory(final String club, final Map<String, List<String>> gender_eligibility_map, final EntryCategory entry_category, final PrizeCategory prize_category) {

        return isResultEligibleForPrizeCategoryByGender(gender_eligibility_map, entry_category, prize_category) &&
            isResultEligibleForPrizeCategoryByAge(entry_category, prize_category) &&
            isResultEligibleForPrizeCategoryByClub(club, prize_category);
    }

    private boolean isResultEligibleForPrizeCategoryByClub(final String club, final PrizeCategory prize_category) {

        final Set<String> eligible_clubs = prize_category.getEligibleClubs();

        if (club == null || eligible_clubs.isEmpty()) return true;

        return eligible_clubs.contains(club);
    }

    /** Tests whether the given entry category is eligible in any of the given prize categories. */
    public boolean isResultEligibleInSomePrizeCategory(final String club, final Map<String, List<String>> gender_eligibility_map, final EntryCategory entry_category, final List<PrizeCategory> prize_categories) {

        return prize_categories.stream().
            anyMatch(category -> isResultEligibleForPrizeCategory(club, gender_eligibility_map, entry_category, category));
    }

    private boolean isResultEligibleForPrizeCategoryByGender(final Map<String, List<String>> gender_eligibility_map, final EntryCategory entry_category, final PrizeCategory prize_category) {

        // It's possible for the entry category to be null in a series race, where some of the individual
        // race results may not include entry categories.

        if (entry_category == null) return false;

        return gender_eligibility_map.keySet().stream().
            filter(entry_gender -> entry_category.getGender().equals(entry_gender)).
            anyMatch(entry_gender -> gender_eligibility_map.get(entry_gender).contains(prize_category.getGender()));
    }

    private static boolean isResultEligibleForPrizeCategoryByAge(final EntryCategory entry_category, final PrizeCategory prize_category) {

        // It's possible for the entry category to be null in a series race, where some of the individual
        // race results may not include entry categories.

        return entry_category != null &&
            entry_category.getMinimumAge() >= prize_category.getMinimumAge() &&
            entry_category.getMaximumAge() <= prize_category.getMaximumAge();
    }
}

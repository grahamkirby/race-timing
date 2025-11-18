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

import org.grahamkirby.race_timing.common.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.util.Comparator.comparingInt;
import static org.grahamkirby.race_timing.common.Config.*;

public class CategoriesProcessor  {

    // TODO document constraints on category overlap and generality.

    /** Index of prize category group name within the relevant config file. */
    public static final int PRIZE_CATEGORY_GROUP_NAME_INDEX = 6;

    public Comparator<PrizeCategory> getDecreasingGeneralityCategoryComparator() {

        return comparingInt((PrizeCategory category) -> category.getMinimumAge()).
            thenComparingInt(category -> GENDER_ORDER.indexOf(category.getGender()));
    }

    // TODO tidy treatment of category configuration files.
    // TODO integrate with category configuration files.
    protected static final List<String> GENDER_ORDER = Arrays.asList("Open", "Women", "Mixed");

    private final List<PrizeCategoryGroup> prize_category_groups;
    private final List<EntryCategory> entry_categories;

    public CategoriesProcessor(final Config config) throws IOException {

        final Path results_path = config.getPath(KEY_ENTRY_CATEGORIES_PATH);
        final Path categories_prize_path = config.getPath(KEY_PRIZE_CATEGORIES_PATH);

        prize_category_groups = new ArrayList<>();
        entry_categories = Files.readAllLines(results_path).stream().filter(line -> !line.startsWith(COMMENT_SYMBOL)).map(EntryCategory::new).toList();

        loadPrizeCategoryGroups(categories_prize_path);
    }

    /** Loads prize category groups from the given file. */
    private void loadPrizeCategoryGroups(final Path prize_categories_path) throws IOException {

        Files.readAllLines(prize_categories_path).stream().
            filter(line -> !line.startsWith(COMMENT_SYMBOL)).
            forEachOrdered(this::recordGroup);
    }

    private void recordGroup(final String line) {

        final String group_name = line.split(",")[PRIZE_CATEGORY_GROUP_NAME_INDEX];
        final PrizeCategoryGroup group = getGroupByName(group_name);

        group.categories().add(new PrizeCategory(line));
    }

    private PrizeCategoryGroup getGroupByName(final String group_name) {

        return prize_category_groups.stream().
            filter(g -> g.group_title().equals(group_name)).
            findFirst().
            orElseGet(() -> newGroup(group_name));
    }

    private PrizeCategoryGroup newGroup(final String group_name) {

        final PrizeCategoryGroup group = new PrizeCategoryGroup(group_name, new ArrayList<>());
        prize_category_groups.add(group);
        return group;
    }

    public List<PrizeCategory> getPrizeCategories() {

        return prize_category_groups.stream().
            flatMap(group -> group.categories().stream()).
            toList();
    }

    public List<PrizeCategoryGroup> getPrizeCategoryGroups() {
        return prize_category_groups;
    }

    public EntryCategory lookupEntryCategory(final String short_name) {

        return entry_categories.stream().
            filter(category -> category.getShortName().equals(short_name)).
            findFirst().
            orElseThrow();
    }

    /** Tests whether the given entry category is eligible for the given prize category. */
    public boolean isResultEligibleForPrizeCategory(final String club, final Map<String, List<String>> gender_eligibility_map, final EntryCategory entry_category, final PrizeCategory prize_category) {

        return isResultEligibleForPrizeCategoryByGender(gender_eligibility_map, entry_category, prize_category) &&
            isResultEligibleForPrizeCategoryByAge(entry_category, prize_category) &&
            isResultEligibleForPrizeCategoryByClub(club, prize_category);
    }

    private boolean isResultEligibleForPrizeCategoryByClub(final String club, final PrizeCategory prize_category) {

        final Set<String> eligible_clubs = prize_category.getEligibleClubs();

        return club == null || eligible_clubs.isEmpty() || eligible_clubs.contains(club);
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

    private boolean isResultEligibleForPrizeCategoryByAge(final EntryCategory entry_category, final PrizeCategory prize_category) {

        // It's possible for the entry category to be null in a series race, where some of the individual
        // race results may not include entry categories.

        return entry_category != null &&
            entry_category.getMinimumAge() >= prize_category.getMinimumAge() &&
            entry_category.getMaximumAge() <= prize_category.getMaximumAge();
    }
}

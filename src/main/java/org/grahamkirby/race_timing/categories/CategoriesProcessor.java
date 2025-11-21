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
import java.nio.file.Path;
import java.util.*;

import static java.util.Comparator.comparingInt;
import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.RaceConfigValidator.readAllLines;

public class CategoriesProcessor  {

    // TODO document constraints on category overlap and generality.

    /** Index of prize category group name within the relevant config file. */
    private static final int PRIZE_CATEGORY_GROUP_NAME_INDEX = 6;

    private List<PrizeCategoryGroup> prize_category_groups;
    private final List<EntryCategory> entry_categories;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public CategoriesProcessor(final Config config) throws IOException {

        final Path entry_categories_path = config.getPath(KEY_ENTRY_CATEGORIES_PATH);
        final Path prize_categories_path = config.getPath(KEY_PRIZE_CATEGORIES_PATH);

        entry_categories = readAllLines(entry_categories_path).stream().
            filter(line -> !line.startsWith(COMMENT_SYMBOL)).
            map(EntryCategory::new).
            toList();

        loadPrizeCategoryGroups(prize_categories_path);
        validatePrizeCategoryGroups();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public EntryCategory getEntryCategory(final String short_name) {

        return entry_categories.stream().
            filter(category -> category.getShortName().equals(short_name)).
            findFirst().
            orElseThrow();
    }

    public List<PrizeCategory> getPrizeCategories() {

        return prize_category_groups.stream().
            flatMap(group -> group.categories().stream()).
            toList();
    }

    public List<PrizeCategoryGroup> getPrizeCategoryGroups() {
        return prize_category_groups;
    }

    /** Tests whether the given entry category is eligible for the given prize category. */
    public boolean isResultEligibleForPrizeCategory(final EntryCategory entry_category, final PrizeCategory prize_category, final String club) {

        return isResultEligibleForPrizeCategoryByGender(entry_category, prize_category) &&
            isResultEligibleForPrizeCategoryByAge(entry_category, prize_category) &&
            isResultEligibleForPrizeCategoryByClub(prize_category, club);
    }

    public List<PrizeCategory> getPrizeCategoriesInDecreasingGeneralityOrder() {

        final Comparator<PrizeCategory> comparator = comparingInt((PrizeCategory category) -> category.getAgeRange().getMinimumAge()).
            thenComparing(comparingInt((PrizeCategory category) -> category.getEligibleGenders().size()).reversed());

        final List<PrizeCategory> categories = makeMutableCopy(getPrizeCategories());
        categories.sort(comparator);

        return categories;
    }

    /** Tests whether the given entry category is eligible in any of the given prize categories. */
    public boolean isResultEligibleInSomePrizeCategory(final String club, final EntryCategory entry_category, final List<PrizeCategory> prize_categories) {

        return prize_categories.stream().
            anyMatch(category -> isResultEligibleForPrizeCategory(entry_category, category, club));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void validatePrizeCategoryGroups() {

        for (final String gender : getPrizeGenderLists()) {

            checkAgeRangeIntersection(gender);
            checkAgeRangeCoverage(gender);
        }
    }

    private void checkAgeRangeIntersection(final String gender) {

        final List<PrizeCategory> categories_for_gender = getPrizeCategories2(gender);

        for (final PrizeCategory category1 : categories_for_gender)
            for (final PrizeCategory category2 : categories_for_gender)
                if (category1.getAgeRange().intersectsWith(category2.getAgeRange()) && category1.isExclusive() && category2.isExclusive())
                    throw new RuntimeException("invalid intersecting age ranges: " +
                        category1.getShortName() + " (" + category1.getAgeRange().getMinimumAge() + "," + category1.getAgeRange().getMaximumAge() + "), " +
                        category2.getShortName() + " (" + category2.getAgeRange().getMinimumAge() + "," + category2.getAgeRange().getMaximumAge() + ")");
    }

    private void checkAgeRangeCoverage(final String gender) {

        final List<AgeRange> amalgamated_ranges = getAmalgamatedAgeRanges(gender);

        if (amalgamated_ranges.size() > 1)
            throw new RuntimeException("invalid categories: missing age range for " + gender + ": (" +
                (amalgamated_ranges.get(0).getMaximumAge() + 1) + "," + (amalgamated_ranges.get(1).getMinimumAge() - 1) + ")");
    }

    private List<AgeRange> getAmalgamatedAgeRanges(final String gender) {

        final List<AgeRange> ranges = makeMutableCopy(
            getPrizeCategories2(gender).stream().
                map(Category::getAgeRange).
                toList());

        amalgamate(ranges);
        ranges.sort(Comparator.comparingInt(AgeRange::getMinimumAge));

        return ranges;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private void amalgamate(final List<AgeRange> ranges) {

        while (amalgamateOnePair(ranges)) {}
    }

    private boolean amalgamateOnePair(final List<AgeRange> ranges) {

        for (final AgeRange range1 : ranges)
            for (final AgeRange range2 : ranges)
                if (canBeAmalgamated(range1, range2)) {

                    ranges.remove(range1);
                    ranges.remove(range2);

                    ranges.add(amalgamate(range1, range2));
                    return true;
                }

        return false;
    }

    private boolean canBeAmalgamated(final AgeRange range1, final AgeRange range2) {

        return range1 != range2 &&
            (range1.getMaximumAge() + 1 == range2.getMinimumAge() ||
             range2.getMinimumAge() >= range1.getMinimumAge() && range2.getMaximumAge() <= range1.getMaximumAge());
    }

    private AgeRange amalgamate(final AgeRange range1, final AgeRange range2) {

        return new AgeRange(
            Math.min(range1.getMinimumAge(), range2.getMinimumAge()),
            Math.max(range1.getMaximumAge(), range2.getMaximumAge()));
    }

    private List<PrizeCategory> getPrizeCategories2(final String gender_list) {

        return getPrizeCategories().stream().
            filter(category -> getEligibleGenderList(category).equals(gender_list)).
            toList();
    }

    private List<String> getPrizeGenderLists() {

        return getPrizeCategories().stream().
            map(CategoriesProcessor::getEligibleGenderList).
            distinct().
            toList();
    }

    private static String getEligibleGenderList(final PrizeCategory category) {

        return String.join("/", category.getEligibleGenders());
    }

    /** Loads prize category groups from the given file. */
    private void loadPrizeCategoryGroups(final Path prize_categories_path) throws IOException {

        prize_category_groups = new ArrayList<>();

        readAllLines(prize_categories_path).stream().
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

    private boolean isResultEligibleForPrizeCategoryByClub(final PrizeCategory prize_category, final String club) {

        final Set<String> eligible_clubs = prize_category.getEligibleClubs();

        return club == null || eligible_clubs.isEmpty() || eligible_clubs.contains(club);
    }

    private boolean isResultEligibleForPrizeCategoryByGender(final EntryCategory entry_category, final PrizeCategory prize_category) {

        // It's possible for the entry category to be null in a series race, where some of the individual
        // race results may not include entry categories.

        return entry_category != null && prize_category.getEligibleGenders().contains(entry_category.getGender());
    }

    private boolean isResultEligibleForPrizeCategoryByAge(final EntryCategory entry_category, final PrizeCategory prize_category) {

        // It's possible for the entry category to be null in a series race, where some of the individual
        // race results may not include entry categories.

        return entry_category != null &&
            entry_category.getAgeRange().getMinimumAge() >= prize_category.getAgeRange().getMinimumAge() &&
            entry_category.getAgeRange().getMaximumAge() <= prize_category.getAgeRange().getMaximumAge();
    }
}

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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Comparator.comparingInt;
import static org.grahamkirby.race_timing.common.Config.*;

public final class CategoriesProcessor  {

    //////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Each participant (individual or team) is assigned a single entry category. An entry category has an age range and
    // a gender. Age range bounds are inclusive. For example, ranges for consecutive categories might be 40-49 and
    // 50-59. Genders are represented as strings, so can be configured per-race.
    //
    // A prize category has an age range and a set of eligible genders. For example an open prize category might be
    // available to Female, Male and Non-Binary entry category genders.
    //
    // For both entry and prize categories, the age ranges for categories for a given gender (or set of genders) must
    // not intersect, and must cover the whole range (i.e. no gaps). The exception to the above is that one prize
    // category age range can wholly contain another, e.g. an open age range may include all the narrower age ranges.
    //
    // The order in which prize categories are listed in the configuration file determines (only) the order in which
    // prizes are listed in the results.
    //
    // If a participant is eligible for more than one prize (e.g. 1st V40, 2nd Open) then by default they are only
    // awarded the prize in the more general age category (2nd Open in this example), on the assumption that this is
    // more prestigious. This can be overridden by setting PREFER_LOWER_PRIZE_IN_MORE_GENERAL_CATEGORY = false in the
    // race configuration file.
    //
    // Generality ordering of age categories is defined in terms of range containment: age range A is more general than
    // age range B if A completely contains B.
    //
    // The theme of preferring more general prize category when deciding exclusive awards also applies to gender. Gender
    // eligibility ordering by generality assumes that categories with a greater number of eligible genders are more
    // general. This works when every category that is available to more than one gender is available to all genders,
    // e.g. 'Open' categories. It might not work with a more complex gender eligibility structure.
    //
    // If generality of age category conflicts with generality of gender, the former takes priority. For example, if a
    // participant is eligible for prizes in both categories 20-99/Female and 40-49/Female/Male, they receive the former.
    //
    // The default of awarding no more than one prize to a participant can also be overridden by setting the 'exclusive'
    // field of a prize category to false. This then means that a participant can win in that category and any other.
    // Common use cases are for an additional prize category only open to the organising club or to local runners, or
    // where a veteran can win in their age category and in an overall open category. Less commonly the veteran age
    // categories may not be disjoint, e.g. 40+ (40-99), 50+ (50-99) rather than 40-49, 50-59 etc. In this it would be
    // possible for a 50+ participant to win in both 40+ and 50+ categories.
    //
    // Each prize category is defined to belong to a particular named group. Prize category groups are used to structure
    // results output. For example, in a a junior race, runners in different age categories may complete different
    // courses, so it would be meaningless to list their results together. This can be handled by defining prize
    // categories 'Female Under 9' and 'Male Under 9' within the group 'Under 9'. Results for each group are then output
    // within a different section.
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////

    private final List<EntryCategory> entry_categories;
    private final List<PrizeCategory> prize_categories;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public CategoriesProcessor(final Config config) throws IOException {

        final Path entry_categories_path = config.getPath(KEY_ENTRY_CATEGORIES_PATH);
        final Path prize_categories_path = config.getPath(KEY_PRIZE_CATEGORIES_PATH);

        entry_categories = loadCategories(entry_categories_path, EntryCategory::new);
        prize_categories = loadCategories(prize_categories_path, PrizeCategory::new);

        validateCategories(entry_categories, EntryCategory::getGender);
        validateCategories(prize_categories, this::getEligibleGenderList);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public EntryCategory getEntryCategory(final String short_name) {

        return entry_categories.stream().
            filter(category -> category.getShortName().equals(short_name)).
            findFirst().
            orElseThrow();
    }

    public List<PrizeCategory> getPrizeCategories() {

        return prize_categories;
    }

    /** Tests whether the given entry category is eligible for the given prize category. */
    public boolean isResultEligibleForPrizeCategory(final EntryCategory entry_category, final String club, final PrizeCategory prize_category) {

        return isResultEligibleForPrizeCategoryByGender(entry_category, prize_category) &&
            isResultEligibleForPrizeCategoryByAge(entry_category, prize_category) &&
            isResultEligibleForPrizeCategoryByClub(club, prize_category);
    }

    public List<PrizeCategory> getPrizeCategoriesInDecreasingGeneralityOrder() {

        final List<PrizeCategory> categories = makeMutableCopy(getPrizeCategories());
        categories.sort(CategoriesProcessor::comparePrizeCategory);
        return categories;
    }

    /** Tests whether the given entry category is eligible in any of the given prize categories. */
    public boolean isResultEligibleInSomePrizeCategory(final EntryCategory entry_category, final String club, final List<PrizeCategory> prize_categories) {

        return prize_categories.stream().
            anyMatch(prize_category -> isResultEligibleForPrizeCategory(entry_category, club, prize_category));
    }

    public List<String> getPrizeCategoryGroups() {

        return prize_categories.stream().
            map(PrizeCategory::getGroup).
            distinct().
            toList();
    }

    public List<PrizeCategory> getPrizeCategoriesByGroup(final String group) {

        return prize_categories.stream().
            filter(category -> category.getGroup().equals(group)).
            toList();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private <C extends Category> void validateCategories(final List<C> categories, final Function<C, String> extract_gender) {

        checkCategoryNameDuplication(categories);

        for (final String gender : getCategoryGenders(categories, extract_gender)) {

            final List<C> gender_categories = getCategoriesByGender(categories, gender, extract_gender);

            checkAgeRangeIntersection(gender_categories);
            checkAgeRangeCoverage(gender_categories, gender);
        }
    }

    private <C extends Category> void checkCategoryNameDuplication(final List<C> categories) {

        checkCategoryNameDuplication(categories, Category::getShortName);
        checkCategoryNameDuplication(categories, Category::getLongName);
    }

    private static <C extends Category> void checkCategoryNameDuplication(final List<C> categories, final Function<C, String> get_name) {

        final Set<String> seen = new HashSet<>();

        categories.stream().
            map(get_name).
            filter(Predicate.not(seen::add)).
            findFirst().
            ifPresent(name -> { throw new RuntimeException("duplicated category name: " + name); });
    }

    private <C extends Category> List<String> getCategoryGenders(final List<C> categories, final Function<C, String> extract_gender) {

        return categories.stream().
            map(extract_gender).
            toList();
    }

    private String getEligibleGenderList(final PrizeCategory category) {

        return String.join("/", category.getEligibleGenders());
    }

    private <C extends Category> List<C> loadCategories(final Path entry_categories_path, final Function<String, C> make_category) throws IOException {

        return readAllLines(entry_categories_path).stream().
            filter(line -> !line.startsWith(COMMENT_SYMBOL)).
            map(make_category).
            toList();
    }

    private boolean isResultEligibleForPrizeCategoryByClub(final String club, final PrizeCategory prize_category) {

        final Set<String> eligible_clubs = prize_category.getEligibleClubs();

        return eligible_clubs.isEmpty() || eligible_clubs.contains(club);
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

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static int comparePrizeCategory(final PrizeCategory category1, final PrizeCategory category2) {

        return category1.compareTo(category2);
    }

    private <C extends Category> void checkAgeRangeIntersection(final List<C> categories) {

        for (final C category1 : categories)
            for (final C category2 : categories)
                if (category1 != category2 && category1.intersectsWith(category2))
                    throw new RuntimeException("invalid intersecting age ranges: " + category1 + ", " + category2);
    }

    private <C extends Category> List<C> getCategoriesByGender(final List<C> categories, final String gender, final Function<C, String> extract_gender) {

        return categories.stream().
            filter(category -> extract_gender.apply(category).equals(gender)).
            toList();
    }

    private <C extends Category> void checkAgeRangeCoverage(final List<C> categories, final String gender) {

        final List<AgeRange> amalgamated_ranges = getAmalgamatedAgeRanges(categories);

        if (amalgamated_ranges.size() > 1)
            throw new RuntimeException("invalid categories: missing age range for " + gender + ": (" +
                (amalgamated_ranges.get(0).getMaximumAge() + 1) + "," + (amalgamated_ranges.get(1).getMinimumAge() - 1) + ")");
    }

    private <C extends Category> List<AgeRange> getAmalgamatedAgeRanges(final List<C> categories) {

        final List<AgeRange> ranges = makeMutableCopy(
            categories.stream().
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
}

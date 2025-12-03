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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import static java.util.Comparator.comparingInt;
import static org.grahamkirby.race_timing.common.Config.*;

public class CategoriesProcessor  {

    // TODO document constraints on category overlap and generality.
    // TODO document exact meaning of exclusive, maybe rename.

    // Each participant (individual or team) is assigned a single entry category.
    // An entry category has an age range and a gender. Age range bounds are
    // inclusive. For example, ranges for consecutive categories might be 40-49 and 50-59.
    // Genders are represented as strings, so can be configured per-race.
    //
    // A prize category has an age range and a set of eligible genders. For example
    // an open prize category might be available to Female, Male and Non-Binary entry
    // category genders.
    //
     // entry categories can't overlap, and age categories must cover whole range
    // for each gender
    //
     // order of prize categories in config file determines order of prize listings


// * Represents a grouping of prize categories, used in some cases to structure results output.
// * For example, the 'Under 9' group in a junior race may include prize categories
// * 'Female Under 9' and 'Male Under 9'. The results may be split into different groups where
// * runners of different ages complete different courses.


    // age range & gender eligibility ordering determines which prize
    // awarded if more than 1 applicable
    // can be overridden to award higher prize in less general category

    // The gender eligibility ordering assumes that categories with a greater number of eligible
    // genders are more general. This works when every category that is available to more than one gender
    // is available to all genders, e.g. 'Open' categories. It might not work with a more complex
    // gender eligibility structure.

    private final List<EntryCategory> entry_categories;
    private final List<PrizeCategory> prize_categories;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public CategoriesProcessor(final Config config) throws IOException {

        final Path entry_categories_path = config.getPath(KEY_ENTRY_CATEGORIES_PATH);
        final Path prize_categories_path = config.getPath(KEY_PRIZE_CATEGORIES_PATH);

        entry_categories = loadEntryCategories(entry_categories_path);
        prize_categories = loadPrizeCategories(prize_categories_path);

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
    public boolean isResultEligibleForPrizeCategory(final EntryCategory entry_category, final PrizeCategory prize_category, final String club) {

        return isResultEligibleForPrizeCategoryByGender(entry_category, prize_category) &&
            isResultEligibleForPrizeCategoryByAge(entry_category, prize_category) &&
            isResultEligibleForPrizeCategoryByClub(prize_category, club);
    }

    public List<PrizeCategory> getPrizeCategoriesInDecreasingGeneralityOrder() {

        final List<PrizeCategory> categories = makeMutableCopy(getPrizeCategories());
        categories.sort(CategoriesProcessor::comparePrizeCategory);
        return categories;
    }

    /** Tests whether the given entry category is eligible in any of the given prize categories. */
    public boolean isResultEligibleInSomePrizeCategory(final String club, final EntryCategory entry_category, final List<PrizeCategory> prize_categories) {

        return prize_categories.stream().
            anyMatch(category -> isResultEligibleForPrizeCategory(entry_category, category, club));
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
            sorted().
            filter(Predicate.not(seen::add)).
            findFirst().
            ifPresent(name -> { throw new RuntimeException("duplicated category name: " + name); });
    }

    private <C extends Category> List<String> getCategoryGenders(final List<C> categories, final Function<C, String> extract_gender) {

        return categories.stream().
            map(extract_gender).
            distinct().
            toList();
    }

    private String getEligibleGenderList(final PrizeCategory category) {

        return String.join("/", category.getEligibleGenders());
    }

    private List<EntryCategory> loadEntryCategories(final Path entry_categories_path) throws IOException {

        return readAllLines(entry_categories_path).stream().
            filter(line -> !line.startsWith(COMMENT_SYMBOL)).
            map(EntryCategory::new).
            toList();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Loads prize category groups from the given file. */
    private List<PrizeCategory> loadPrizeCategories(final Path prize_categories_path) throws IOException {

        return readAllLines(prize_categories_path).stream().
            filter(line -> !line.startsWith(COMMENT_SYMBOL)).
            map(PrizeCategory::new).
            toList();
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

    //////////////////////////////////////////////////////////////////////////////////////////////////

    // Gender eligibility ordering based only on the number of eligible genders.
    private static int compareByIncreasingGenderCategoryGenerality(final PrizeCategory category1, final PrizeCategory category2) {

        final ToIntFunction<PrizeCategory> get_number_of_genders = category -> category.getEligibleGenders().size();

        return comparingInt(get_number_of_genders).compare(category1, category2);
    }

    private static int compareByDecreasingGenderCategoryGenerality(final PrizeCategory category1, final PrizeCategory category2) {

        return -compareByIncreasingGenderCategoryGenerality(category1, category2);
    }

    private static int compareByDecreasingAgeCategoryGenerality(final PrizeCategory category1, final PrizeCategory category2) {

        final AgeRange range1 = category1.getAgeRange();
        final AgeRange range2 = category2.getAgeRange();

        if (range1.equals(range2)) return 0;
        if (range1.contains(range2)) return -1;   // range1 is more general.
        if (range2.contains(range1)) return 1;    // range1 is less general.

        // Equal generality. The ranges must be disjoint since there's no containment, and intersecting
        // ranges are rejected during category validation.
        return 0;
    }

    private static int comparePrizeCategory(final PrizeCategory category1, final PrizeCategory category2) {

        final int age_comparison = compareByDecreasingAgeCategoryGenerality(category1, category2);
        final int gender_comparison = compareByDecreasingGenderCategoryGenerality(category1, category2);

        // This assumes that both comparators return the same negative or positive numbers.
        final boolean comparison_same_for_both_aspects = age_comparison == gender_comparison;
        final boolean age_comparison_equal = age_comparison == 0;
        final boolean age_comparison_has_priority = !comparison_same_for_both_aspects && !age_comparison_equal;

        return age_comparison_has_priority ? age_comparison : gender_comparison;
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

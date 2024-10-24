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
package org.grahamkirby.race_timing.common.categories;

public final class Categories {

//    public record PrizeCategoryGroup(String combined_categories_title, List<Category> categories){}

    //////////////////////////////////////////////////////////////////////////////////////////////////

//    // TODO rewrite to load from config file. Include both min and max age.
////    private final List<EntryCategory> entry_categories;
////    private final List<PrizeCategory> prize_categories;
////    private final List<PrizeCategoryGroup> prize_category_groups = new ArrayList<>();
//
//    public Categories(final Path entry_categories_file_path, final Path prize_categories_file_path) throws IOException {
//
//
//
////        entry_categories = Files.readAllLines(entry_categories_file_path).stream().map(EntryCategory::new).toList();
////        prize_categories = Files.readAllLines(prize_categories_file_path).stream().map(PrizeCategory::new).toList();
//    }
//
//    // Order not important.
//    public List<EntryCategory> getEntryCategories() {
//        return entry_categories;
//    }
//
//    // Defines the order of listing in prize reports.
//    public List<PrizeCategory> getPrizeCategories() {
//        return prize_categories;
//    }
//
//    public PrizeCategory getPrizeCategory(final String category_short_name) {
//
//        return prize_categories.stream().filter(category -> category.getShortName().equals(category_short_name)).findFirst().orElseThrow(
//                 () -> new RuntimeException("unknown category: " + category_short_name));
//
//    }
//
//    public boolean isEligibleFor(final EntryCategory entry_category, PrizeCategory prize_category) {
//
//        return false;
//    }
//
//    public List<PrizeCategoryGroup> getPrizeCategoryGroups() {
//        return prize_category_groups;
//    }
}

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

import java.util.ArrayList;
import java.util.List;

public abstract class Categories {

    protected final List<Category> runner_categories = new ArrayList<>();
    protected final List<Category> prize_categories_in_decreasing_generality_order = new ArrayList<>();
    protected final List<Category> prize_categories_in_report_order = new ArrayList<>();

    // Order not important.
    public List<Category> getRunnerCategories() {
        return runner_categories;
    }

    // Defines the order of iteration when allocating prizes.
    public List<Category> getPrizeCategoriesInDecreasingGeneralityOrder() {
        return prize_categories_in_decreasing_generality_order;
    }

    // Defines the order of iteration when reporting prizes.
    public List<Category> getPrizeCategoriesInReportOrder() {
        return prize_categories_in_report_order;
    }

    public Category getCategory(final String category_short_name) {

        for (final Category category : runner_categories)
            if (category.getShortName().equals(category_short_name)) return category;

        throw new RuntimeException("unknown category: " + category_short_name);
    }

    public abstract boolean includes(Category first_category, Category second_category);
}

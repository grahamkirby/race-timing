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

public class JuniorRaceCategories extends Categories {

    public JuniorRaceCategories(final int number_of_category_prizes) {

        runner_categories.add(new Category("Female Under 9", "FU9", "Female", 7, number_of_category_prizes));
        runner_categories.add(new Category("Male Under 9", "MU9", "Male", 7, number_of_category_prizes));
        runner_categories.add(new Category("Female Under 11", "FU11", "Female", 9, number_of_category_prizes));
        runner_categories.add(new Category("Male Under 11", "MU11", "Male", 9, number_of_category_prizes));
        runner_categories.add(new Category("Female Under 13", "FU13", "Female", 11, number_of_category_prizes));
        runner_categories.add(new Category("Male Under 13", "MU13", "Male", 11, number_of_category_prizes));
        runner_categories.add(new Category("Female Under 15", "FU15", "Female", 13, number_of_category_prizes));
        runner_categories.add(new Category("Male Under 15", "MU15", "Male", 13, number_of_category_prizes));
        runner_categories.add(new Category("Female Under 18", "FU18", "Female", 15, number_of_category_prizes));
        runner_categories.add(new Category("Male Under 18", "MU18", "Male", 15, number_of_category_prizes));

        prize_categories_in_decreasing_generality_order.addAll(runner_categories);
        prize_categories_in_report_order.addAll(runner_categories);
    }

    public boolean includes(final Category first_category, final Category second_category) {

        // No category includes another.
        return first_category.equals(second_category);
    }
}

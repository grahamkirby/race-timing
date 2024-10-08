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

import java.util.Arrays;

public class SeniorRaceCategories extends Categories {

    public SeniorRaceCategories(final boolean open_prize_categories, final boolean senior_prize_categories, final int number_of_open_prizes, final int number_of_senior_prizes, final int number_of_category_prizes) {

        final Category FU20 = new Category("Women Junior", "FU20", "Women", 0, number_of_category_prizes);
        final Category MU20 = new Category("Men Junior", "MU20", "Men", 0, number_of_category_prizes);

        final Category FS = new Category("Women Senior", "FS", "Women", 20, number_of_senior_prizes);
        final Category MS = new Category("Men Senior", "MS", "Men", 20, number_of_senior_prizes);

        final Category F40 = new Category("Women 40-49", "F40", "Women", 40, number_of_category_prizes);
        final Category M40 = new Category("Men 40-49", "M40", "Men", 40, number_of_category_prizes);
        final Category F50 = new Category("Women 50-59", "F50", "Women", 50, number_of_category_prizes);
        final Category M50 = new Category("Men 50-59", "M50", "Men", 50, number_of_category_prizes);
        final Category F60 = new Category("Women 60-69", "F60", "Women", 60, number_of_category_prizes);
        final Category M60 = new Category("Men 60-69", "M60", "Men", 60, number_of_category_prizes);
        final Category F70 = new Category("Women 70+", "F70+", "Women", 70, number_of_category_prizes);
        final Category M70 = new Category("Men 70+", "M70+", "Men", 70, number_of_category_prizes);

        final Category FO = new Category("Women Open", "FO", "Women", 0, number_of_open_prizes);
        final Category MO = new Category("Men Open", "MO", "Men", 0, number_of_open_prizes);

        runner_categories.addAll(Arrays.asList(FU20, MU20, FS, MS, F40, M40, F50, M50, F60, M60, F70, M70));

        if (open_prize_categories) {
            prize_categories_in_decreasing_generality_order.add(FO);
            prize_categories_in_decreasing_generality_order.add(MO);

            prize_categories_in_report_order.add(FO);
            prize_categories_in_report_order.add(MO);
        }

        if (senior_prize_categories) {
            prize_categories_in_decreasing_generality_order.add(FS);
            prize_categories_in_decreasing_generality_order.add(MS);

            prize_categories_in_report_order.add(FS);
            prize_categories_in_report_order.add(MS);
        }

        prize_categories_in_decreasing_generality_order.addAll(Arrays.asList(FU20, MU20, F40, M40, F50, M50, F60, M60, F70, M70));
        prize_categories_in_report_order.addAll(Arrays.asList(FU20, MU20, F40, M40, F50, M50, F60, M60, F70, M70));
    }

    @Override
    public boolean includes(final Category first_category, final Category second_category) {

        // Only the open categories include any other categories.
        return first_category.equals(second_category) ||
                (first_category.getLongName().equals("Women Open") && second_category.getGender().equals("Women")) ||
                (first_category.getLongName().equals("Men Open") && second_category.getGender().equals("Men"));
    }
}

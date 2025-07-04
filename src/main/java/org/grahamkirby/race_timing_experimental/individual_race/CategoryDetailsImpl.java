/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (graham.kirby@st-andrews.ac.uk)
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
package org.grahamkirby.race_timing_experimental.individual_race;

import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing_experimental.common.CategoryDetails;

import java.util.List;

public class CategoryDetailsImpl implements CategoryDetails {

    private final List<EntryCategory> entry_categories;
    private final List<PrizeCategoryGroup> prize_category_groups;

    public CategoryDetailsImpl(List<EntryCategory> entry_categories, List<PrizeCategoryGroup> prize_category_groups) {

        this.entry_categories = entry_categories;
        this.prize_category_groups = prize_category_groups;
    }

    @Override
    public List<EntryCategory> getEntryCategories() {
        return entry_categories;
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
}

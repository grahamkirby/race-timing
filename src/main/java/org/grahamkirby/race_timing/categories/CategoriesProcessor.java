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
import org.grahamkirby.race_timing.common.Race;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.Config.COMMENT_SYMBOL;

public class CategoriesProcessor  {

    /** Index of prize category group name within the relevant config file. */
    public static final int PRIZE_CATEGORY_GROUP_NAME_INDEX = 6;

    private List<PrizeCategoryGroup> prize_category_groups;
    private Race race;

    public void setRace(Race race) {
        this.race = race;
    }

    public CategoryDetails getCategoryDetails() {

        try {
            final Config config = race.getConfig();
            final Path results_path = (Path) config.get(KEY_ENTRY_CATEGORIES_PATH);
            List<EntryCategory> entry_categories = Files.readAllLines(results_path).stream().filter(line -> !line.startsWith(COMMENT_SYMBOL)).map(EntryCategory::new).toList();
            prize_category_groups = new ArrayList<>();
            final Path categories_prize_path = (Path) race.getConfig().get(KEY_PRIZE_CATEGORIES_PATH);
            loadPrizeCategoryGroups(categories_prize_path);

            return new CategoryDetails(entry_categories, prize_category_groups);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
}

/*
 * Copyright 2025 Graham Kirby:
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

import java.util.List;

/**
 * Represents a grouping of prize categories, used in some cases to structure results output.
 * For example, the 'Under 9' group in a junior race may include prize categories
 * 'Female Under 9' and 'Male Under 9'. The results may be split into different groups where
 * runners of different ages complete different courses.
 * <br />
 * Values are read from a configuration file such as
 * {@link /src/main/resources/configuration/categories_prize_individual_junior.csv}.
 */
public record PrizeCategoryGroup(String group_title, List<PrizeCategory> categories) {
}

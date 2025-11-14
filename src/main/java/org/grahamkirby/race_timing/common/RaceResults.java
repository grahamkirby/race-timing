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
package org.grahamkirby.race_timing.common;

import org.grahamkirby.race_timing.categories.PrizeCategory;
import org.grahamkirby.race_timing.categories.PrizeCategoryGroup;

import java.util.List;

/**
 * Defines aspects of results needed to generate output files.
 */
public interface RaceResults {

    Config getConfig();
    Normalisation getNormalisation();
    Notes getNotes();

    List<? extends RaceResult> getOverallResults();
    List<? extends RaceResult> getOverallResults(List<PrizeCategory> categories);
    List<? extends RaceResult> getPrizeWinners(PrizeCategory category);

    List<String> getTeamPrizes();
    List<PrizeCategoryGroup> getPrizeCategoryGroups();
    boolean arePrizesInThisOrLaterCategory(PrizeCategory prizeCategory);
}

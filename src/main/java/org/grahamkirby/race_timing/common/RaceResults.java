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

import java.util.List;

/**
 * Defines aspects of results needed to generate output files.
 */
public interface RaceResults {

    /**
     * Gets the overall results.
     * @return a list containing the overall results, in rank order
     */
    List<? extends RaceResult> getOverallResults();

    /**
     * Gets the results for the participants eligible for at least one of the given prize categories.
     * @param categories a list of prize categories
     * @return a list containing all results for the given categories, in rank order
     */
    List<? extends RaceResult> getOverallResults(List<PrizeCategory> categories);

    /**
     * Gets the prize winners in the given prize category.
     * @param category the category
     * @return a list containing the results eligible for prizes in the cateogry, with highest prize first
     */
    List<? extends RaceResult> getPrizeWinners(PrizeCategory category);

    /**
     * Gets the names of the prize category groups.
     * @return a list containing the group names
     */
    List<String> getPrizeCategoryGroups();

    /**
     * Gets the prize categories in the given group.
     * @param group the group
     * @return the prize categories in that group
     */
    List<PrizeCategory> getPrizeCategoriesByGroup(String group);

    /**
     * Tests whether prizes have been awarded in the given category or another category later in the prize
     * list order.
     * @param category the category
     * @return true if prizes have been awarded in this or a later category
     */
    boolean arePrizesInThisOrLaterCategory(PrizeCategory category);

    /**
     * Gets the race configuration.
     * @return the configuration
     */
    Config getConfig();

    /**
     * Gets the processor for normalising names, clubs etc.
     * @return the normalisation processor
     */
    NormalisationProcessor getNormalisationProcessor();

    /**
     * Gets the processor for notes.
     * @return the notes processor
     */
    NotesProcessor getNotesProcessor();
}

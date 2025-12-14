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

import org.grahamkirby.race_timing.categories.CategoriesProcessor;

import java.io.IOException;

/**
 * View of race exposing internal details, used in calculating results.
 */
public interface RaceInternal extends Race {

    /**
     * Initialises race internal state.
     * @throws IOException if some race data cannot be read
     */
    void initialise() throws IOException;

    /**
     * Sets the output details for the race.
     * @param output the output
     */
    void setOutput(RaceOutput output);

    /**
     * Gets the race configuration.
     * @return the configuration
     */
    Config getConfig();

    /**
     * Gets the processor for entry and prize categories.
     * @return the categories processor
     */
    CategoriesProcessor getCategoriesProcessor();

    /**
     * Gets the processor for results.
     * @return the results processor
     */
    RaceResultsProcessor getResultsProcessor();

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

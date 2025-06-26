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


import org.grahamkirby.race_timing_experimental.common.Race;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

/** Abstracts over the details of how to print out race results. */
@SuppressWarnings("IncorrectFormatting")
public abstract class ResultPrinter {

    protected final Race race;
    protected final OutputStreamWriter writer;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected ResultPrinter(final Race race, final OutputStreamWriter writer) {
        this.race = race;
        this.writer = writer;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract void printResult(IndividualRaceResult result) throws IOException;

    protected void printResultsHeader() throws IOException {
    }

    protected void printResultsFooter() throws IOException {
    }

    protected void printNoResults() throws IOException {
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Prints out the given list of results. */
    @SuppressWarnings("TypeMayBeWeakened")
    public final void print(final List<? extends IndividualRaceResult> results) throws IOException {

        if (results.isEmpty())
            printNoResults();

        else {
            printResultsHeader();

            for (final IndividualRaceResult result : results)
                printResult(result);

            printResultsFooter();
        }
    }
}

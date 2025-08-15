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
package org.grahamkirby.race_timing.single_race;


import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceInput;

import java.util.List;

import static org.grahamkirby.race_timing_experimental.common.Config.COMMENT_SYMBOL;

@SuppressWarnings({"IncorrectFormatting", "AbstractMethodCallInConstructor"})
public abstract class SingleRaceInput extends RaceInput {

    protected SingleRaceInput(final Race race) {

        super(race);
        readProperties();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract void readProperties();
    protected abstract void validateEntries();
    protected abstract SingleRaceEntry makeRaceEntry(final List<String> elements);

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public void validateInputFiles() {

        validateEntries();
    }

    public static String stripComment(final String line) {

        return line.split(COMMENT_SYMBOL)[0];
    }

    public static String stripEntryComment(final String line) {

        return line.startsWith(COMMENT_SYMBOL) ? "" : line;
    }
}

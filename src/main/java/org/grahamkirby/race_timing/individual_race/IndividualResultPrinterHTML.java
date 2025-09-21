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
package org.grahamkirby.race_timing.individual_race;


import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.ResultPrinterHTML;
import org.grahamkirby.race_timing.common.SingleRaceResult;

import java.io.OutputStreamWriter;
import java.util.List;

import static org.grahamkirby.race_timing.common.Config.DNF_STRING;
import static org.grahamkirby.race_timing.common.Config.renderDuration;

/** Base class for printing results to HTML files. */
public abstract class IndividualResultPrinterHTML extends ResultPrinterHTML {

    protected IndividualResultPrinterHTML(final Race race, final OutputStreamWriter writer) {
        super(race, writer);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected List<String> getResultsElements(final RaceResult r) {

        final SingleRaceResult result = (SingleRaceResult) r;

        return List.of(
            result.position_string,
            String.valueOf(result.entry.bib_number),
            race.getNormalisation().htmlEncode(result.entry.participant.name),
            ((Runner) result.entry.participant).club,
            result.entry.participant.category.getShortName(),
            renderDuration(result, DNF_STRING)
        );
    }
}

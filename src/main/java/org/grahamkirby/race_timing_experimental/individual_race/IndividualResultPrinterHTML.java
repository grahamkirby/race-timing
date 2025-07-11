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


import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing_experimental.common.Race;
import org.grahamkirby.race_timing_experimental.common.RaceResult;
import org.grahamkirby.race_timing_experimental.common.ResultPrinterHTML;
import org.grahamkirby.race_timing_experimental.common.SingleRaceResult;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import static org.grahamkirby.race_timing.common.Race.LINE_SEPARATOR;
import static org.grahamkirby.race_timing_experimental.individual_race.IndividualRaceOutputCSV.renderDuration;
import static org.grahamkirby.race_timing_experimental.individual_race.IndividualRaceResultsOutput.DNF_STRING;

/** Base class for printing results to HTML files. */
public abstract class IndividualResultPrinterHTML extends ResultPrinterHTML {

    protected IndividualResultPrinterHTML(final Race race, final OutputStreamWriter writer) {
        super(race, writer);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////


    protected List<String> getResultsElements(final RaceResult r) {

        SingleRaceResult result = (SingleRaceResult)r;
        return List.of(
            result.position_string,
            String.valueOf(result.entry.bib_number),
            race.getNormalisation().htmlEncode(result.entry.participant.name),
            ((Runner)result.entry.participant).club,
            result.entry.participant.category.getShortName(),
            renderDuration(result, DNF_STRING)
        );
    }
}

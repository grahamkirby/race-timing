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


import org.grahamkirby.race_timing.common.*;

import java.io.OutputStreamWriter;
import java.util.List;

import static org.grahamkirby.race_timing.common.Config.DNF_STRING;
import static org.grahamkirby.race_timing.common.Normalisation.*;

/** Base class for printing results to HTML files. */
public abstract class SingleRaceOutputPrinterHTML extends OverallResultPrinterHTML {

    protected SingleRaceOutputPrinterHTML(final Race race, final OutputStreamWriter writer) {
        super(race, writer);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected List<String> getResultsElements(final RaceResult r) {

        final SingleRaceResult result = (SingleRaceResult) r;

        return List.of(
            result.getPositionString(),
            String.valueOf(result.bib_number),
            race.getNormalisation().htmlEncode(result.getParticipant().name),
            ((Runner) result.getParticipant()).club,
            result.getParticipant().category.getShortName(),
            renderDuration(result, DNF_STRING)
        );
    }
}

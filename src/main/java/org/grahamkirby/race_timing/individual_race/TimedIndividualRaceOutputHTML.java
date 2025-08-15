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
package org.grahamkirby.race_timing.individual_race;


import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.output.RaceOutputHTML;
import org.grahamkirby.race_timing.common.output.ResultPrinter;
import org.grahamkirby.race_timing.common.output.ResultPrinterHTML;
import org.grahamkirby.race_timing.single_race.SingleRaceResult;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import static org.grahamkirby.race_timing_experimental.common.Config.DNF_STRING;
import static org.grahamkirby.race_timing_experimental.common.Config.LINE_SEPARATOR;


class TimedIndividualRaceOutputHTML extends RaceOutputHTML {

    TimedIndividualRaceOutputHTML(final TimedRace race) {
        super(race);
    }

    @Override
    protected ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer) {
        return new OverallResultPrinter(race, writer);
    }

    @Override
    protected ResultPrinter getPrizeResultPrinter(final OutputStreamWriter writer) {
        return new PrizeResultPrinter(race, writer);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OverallResultPrinter extends ResultPrinterHTML {

        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        protected List<String> getResultsColumnHeaders() {

            return List.of("Pos", "No", "Runner", "Club", "Category", "Time");
        }

        @Override
        protected List<String> getResultsElements(final RaceResult r) {

            final SingleRaceResult result = ((SingleRaceResult) r);

            return List.of(
                result.position_string,
                String.valueOf(result.entry.bib_number),
                race.normalisation.htmlEncode(result.entry.participant.name),
                ((Runner)result.entry.participant).club,
                result.entry.participant.category.getShortName(),
                renderDuration(result, DNF_STRING)
            );
        }
    }

    private static final class PrizeResultPrinter extends ResultPrinterHTML {

        private PrizeResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResultsHeader() throws IOException {

            writer.append("<ul>").append(LINE_SEPARATOR);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final SingleRaceResult result = ((SingleRaceResult) r);

            writer.append(STR."    <li>\{result.position_string} \{race.normalisation.htmlEncode(result.entry.participant.name)} (\{((Runner)result.entry.participant).club}) \{renderDuration(result)}</li>\n");
        }

        @Override
        public void printResultsFooter() throws IOException {

            writer.append("</ul>").append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        }
    }
}

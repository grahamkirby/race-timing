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
package org.grahamkirby.race_timing.common.output;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing.series_race.tour.TourRaceResult;
import org.grahamkirby.race_timing.single_race.SingleRaceResult;

import java.time.Duration;

import static org.grahamkirby.race_timing.common.Normalisation.SUFFIX_CSV;
import static org.grahamkirby.race_timing.common.Normalisation.format;

/** Base class for CSV output. */
public abstract class RaceOutputCSV extends RaceOutput {

    protected RaceOutputCSV(final Race race) {
        super(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Encodes a single value by surrounding with quotes if it contains a comma. */
    protected static String encode(final String s) {
        return s.contains(",") ? STR."\"\{s}\"" : s;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getFileSuffix() {
        return SUFFIX_CSV;
    }

    /** No headings in CSV file. */
    @Override
    protected String getResultsHeader() {
        return "";
    }

    /** No headings in CSV file. */
    @Override
    protected String getResultsSubHeader(final String s) {
        return "";
    }

    /** No headings in CSV file. */
    @Override
    public String getPrizesHeader() {
        return "";
    }

    /** No headings in CSV file. */
    @Override
    public String getPrizeCategoryHeader(final PrizeCategory category) {
        return "";
    }

    /** No footers in CSV file. */
    @Override
    public String getPrizeCategoryFooter() {
        return "";
    }
}
